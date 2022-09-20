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
package gov.sandia.gnem.dbutillib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class manages configuration parameters. Parameters are essentially name-value pairs. Note that parameter names
 * are case insensitive and '_' (underscore) characters are ignored, e.g., UserName, user_name and username are all
 * equivalent parameter names.
 * <p>
 * An instance of a ParInfo object knows about several default values that it retrieves from system variables. These are
 * UserName, Password, Instance, Driver, TableDefinitionTable, DAOType, Table Tablespace, and Index Tablespace. If a
 * parameter item value is requested using getItem(parName) and parName is not currently a member of the parameter set,
 * then ParInfo searches its map of default system variables to see if there is a match. For a match to succeed, it is
 * sufficient for parName to end with name of the default parameter name. For example, if the user requests the value
 * that is associated with SourceUser_Name, and ParInfo has a value associated with username, then the default value is
 * returned.
 */
public class ParInfo {
    /**
     * A map associating (String) parameter name -> (String) parameter value. Parameter names are lower case and have
     * had '_' stripped out.
     */
    protected HashMap<String, String> parameters = new HashMap<String, String>();

    /**
     * A map from the original name to the 'reduced' name of parameters. The original name for a parameter is the name
     * that the parameter has in the parameter file. The 'reduced' name is the name as it appears in the parameters
     * hashmap (all lower case and '_' removed).
     */
    protected TreeMap<String, String> originalName = new TreeMap<String, String>();

    /**
     * Separator between name and value in par files. Note that this is public so applications can change this if they
     * want.
     */
    public String delimeter = "=";

    /**
     * String that begins a comment. Lines that start with this are ignored and any part of a line that comes after this
     * is ignored.
     */
    public String commentChar = "//";

    /**
     * Filter - lines in the parameter file that gets read into this ParInfo object that do not start with this filter
     * are ignored.
     */
    private String filter = null;

    /**
     * If parameters came from a file, this is the name of the file they came from.
     */
    private String parFileName = null;

    /**
     * HashMap of name -> default value pairs. Currently, defaults are available for username, password, instance,
     * driver, tabledefinitiontable, daotype, table tablespace, and index tablespace.
     */
    protected HashMap<String, String> defaults = new HashMap<String, String>();

    /**
     * Default constructor. Essenially sets up the default parameters obtained from the system/user's environment.
     */
    public ParInfo() {
        if (DBDefines.DEFAULT_USERNAME != null && DBDefines.DEFAULT_USERNAME.length() > 0)
            defaults.put("username", DBDefines.DEFAULT_USERNAME);
        if (DBDefines.DEFAULT_PASSWORD != null && DBDefines.DEFAULT_PASSWORD.length() > 0)
            defaults.put("password", DBDefines.DEFAULT_PASSWORD);
        if (DBDefines.DEFAULT_INSTANCE != null && DBDefines.DEFAULT_INSTANCE.length() > 0)
            defaults.put("instance", DBDefines.DEFAULT_INSTANCE);
        if (DBDefines.DEFAULT_DRIVER != null && DBDefines.DEFAULT_DRIVER.length() > 0)
            defaults.put("driver", DBDefines.DEFAULT_DRIVER);
        if (DBDefines.DEFAULT_TABLEDEF != null && DBDefines.DEFAULT_TABLEDEF.length() > 0)
            defaults.put("tabledefinitiontable", DBDefines.DEFAULT_TABLEDEF);
        if (DBDefines.DEFAULT_DAOTYPE != null && DBDefines.DEFAULT_DAOTYPE.length() > 0)
            defaults.put("daotype", DBDefines.DEFAULT_DAOTYPE);
        if (DBDefines.DEFAULT_TABLE_TABLESPACE != null && DBDefines.DEFAULT_TABLE_TABLESPACE.length() > 0)
            defaults.put("tabletablespace", DBDefines.DEFAULT_TABLE_TABLESPACE);
        if (DBDefines.DEFAULT_INDEX_TABLESPACE != null && DBDefines.DEFAULT_INDEX_TABLESPACE.length() > 0)
            defaults.put("indextablespace", DBDefines.DEFAULT_INDEX_TABLESPACE);

        if (defaults.get("daotype") == null)
            defaults.put("daotype", "DB");
    }

    /**
     * Constructor
     *
     * @param useDefaults whether or not to populate the ParInfo object with default data from the user's environment
     */
    public ParInfo(boolean useDefaults) {
        this();
        if (!useDefaults)
            this.defaults.clear();
    }

    /**
     * Constructor that takes a String as a parameter. If the string contains a newline character (system dependent),
     * then the string is interpreted to be a list of parameter name=value pairs (contents of a par file) and is
     * processed accordingly. If the string does not contain a newline character, it is interpreted to be a file name
     * and the contents of the file are processed.
     *
     * @param init either a file name or the contents of a par file.
     */
    public ParInfo(String init) {
        this();
        if (init.indexOf(DBDefines.EOLN) < 0)
            readParFile(init);
        else if (init.indexOf("\n") < 0)
            readParFile(init);
        else
            addParameters(init);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ParInfo clone() {
        ParInfo p = new ParInfo();
        p.parameters = (HashMap<String, String>) parameters.clone();
        p.originalName = (TreeMap<String, String>) originalName.clone();
        p.delimeter = delimeter;
        p.commentChar = commentChar;
        p.filter = filter;
        p.parFileName = parFileName;
        p.defaults = (HashMap) defaults.clone();
        return p;
    }

    /**
     * Read the contents of a par file into this ParInfo container.
     *
     * @param fname the name of the par file to read.
     * @return true if the file was read successfully.
     */
    public boolean readParFile(String fname) {
        this.parFileName = fname;
        try {
            BufferedReader f = new BufferedReader(new FileReader(fname));
            StringBuilder s = new StringBuilder();
            String line = "";
            while (f.ready()) {
                line = f.readLine().trim();
                // Ignore comments.
                if (!line.startsWith(commentChar)
                        && (this.filter == null || line.toLowerCase().startsWith(this.filter.toLowerCase())))
                    s.append(line + DBDefines.EOLN);
            }
            f.close();
            addParameters(s.toString());
            parFileName = fname;
            return true;
        } catch (IOException e) {
            System.err.println("ERROR in ParInfo.readParFile(" + fname + ") \n" + e.getMessage());
            return false;
        }
    }

    public boolean writeParFile(File file, boolean printPassword) throws IOException {
        if (file == null)
            return false;
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(getConfiguration(printPassword));
        bw.flush();
        bw.close();
        return true;
    }

    public boolean writeParFile(String fileName, boolean printPassword) throws IOException {
        if (fileName == null || fileName.length() == 0)
            return false;
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(getConfiguration(printPassword));
        bw.flush();
        bw.close();
        return true;
    }

    /**
     * Returns an iterator over the parameter names known to this ParInfo object.
     *
     * @return Iterator<String> an iterator over the parameter names known to this ParInfo object.
     */
    public Set<String> keySet() {
        return originalName.keySet();
    }

    /**
     * Add a parameter (specified as a name-value pair) to the ParInfo item. For example, say a user wants to add a
     * parameter named misc to the ParInfo object, and wants misc's value to be miscName, then call this function as
     * follows: <BR>
     * <code>addParameter("misc", "miscName");</code><BR>
     * If the ParInfo object already contains a parameter with that name, the old value is replaced with the new value.
     *
     * @param name  the name of the parameter
     * @param value the value to associate with the name.
     */
    public void addParameter(String name, String value) {
        String plain = reduceParName(name);
        originalName.put(name, plain);
        if (value == null)
            this.parameters.put(plain, value);
        else
            this.parameters.put(plain, filterSystemProperties(value.trim()));
    }

    protected String reduceParName(String parName) {
        return parName.replaceAll("_", "").toLowerCase().trim();
    }

    public void addParameters(ParInfo otherParInfo) {
        // The return String uses the original names ...
        for (String name : otherParInfo.originalName.keySet())
            addParameter(name, otherParInfo.getItem(name));
    }

    /**
     * Add a parameter (specified as a name-value pair) to the ParInfo item only if the value to be assigned is not null
     * nor is it empty. If these conditions hold, then addParameter(name, value) is called.
     *
     * @param name  the name of the parameter
     * @param value the value to associate with the name.
     */
    public void addParameterNoEmpty(String name, String value) {
        if (value != null && value.length() > 0)
            addParameter(name, value);
    }

    /**
     * For each parameter in otherParInfo, see if the parameter exists in this parInfo. If it does, then update the
     * value in this parInfo with the value from otherParInfo.
     *
     * @param otherParInfo ParInfo
     */
    public void updateParameters(ParInfo otherParInfo) {
        for (String parName : otherParInfo.parameters.keySet()) {
            if (parameters.containsKey(parName))
                parameters.put(parName, otherParInfo.parameters.get(parName));
        }
    }

    /**
     * For each parameter in otherParInfo, see if the parameter exists in this parInfo. If it does, then update the
     * value in this parInfo with the value from otherParInfo. If it doesn't, add it.
     *
     * @param otherParInfo ParInfo
     */
    public void updateAndAddParameters(ParInfo otherParInfo) {
        addParameters(otherParInfo);
        updateParameters(otherParInfo);
    }

    public void clear() {
        originalName.clear();
        parameters.clear();
    }

    /**
     * Add a parameter (specified as a name-value pair) to the ParInfo item. For example, say a user wants to add a
     * parameter named misc to the ParInfo object, and wants misc's value to be miscName, then call this function as
     * follows: <BR>
     * <code>appendParameter("misc", "miscName");</code><BR>
     * If the ParInfo object already contains a parameter with that name, the new value is appended onto the end of the
     * existing value, with an intervening newline character.
     *
     * @param name  the name of the parameter
     * @param value the value to associate with name
     */
    public void appendParameter(String name, String value) {
        String plain = reduceParName(name);
        String orig = parameters.get(plain);
        if (orig != null) {
            if (value == null)
                value = orig;
            else
                value = orig + DBDefines.EOLN + filterSystemProperties(value);
        }
        originalName.put(name, plain);
        this.parameters.put(plain, value.trim());
    }

    /**
     * Remove a parameter from the parameter set.
     *
     * @param name String the name of the parameter that is to be removed (case insensitive and underscore characters
     *             are ignored.
     */
    public void removeParameter(String name) {
        String plain = reduceParName(name);
        originalName.remove(name);
        parameters.remove(plain);
    }

    /**
     * Add a parameter (specified as a name-value1 value2 pair) to the ParInfo object. Parameter names are not limited
     * to just one "word" long values - they can be associated with a "many word" value. For example, say a user wants
     * to add have a parameter named <code>misc</code> in the ParInfo object, and wants <code>misc</code>'s value to be
     * <code>miscName miscType
     * </code>, then call this function as follows: <BR>
     * <code>appendParameter("misc", "miscName", "miscType");</code><BR>
     * If the ParInfo object already contains a parameter with that name, the new value is appended onto the end of the
     * existing value, with an intervening newline character.
     *
     * @param name   the name of the parameter
     * @param value1 the first value to associate with name
     * @param value2 the second value to associate with name
     */
    public void appendParameter(String name, String value1, String value2) {
        appendParameter(name, (value1 + " " + value2));
    }

    /**
     * Add a parameter (specified as a name-value1 value2 value3 pair) to the ParInfo object. Parameter names are not
     * limited to just one "word" long values - they can be associated with a "many word" value. For example, say a user
     * wants to add have a parameter named <code>misc</code> in the ParInfo object, and wants <code>misc</code>'s value
     * to be <code>miscName
     * miscType miscInfo </code>, then call this function as follows: <BR>
     * <code>appendParameter("misc", "miscName", "miscType", "miscInfo");
     * </code><BR>
     * If the ParInfo object already contains a parameter with that name, the new value is appended onto the end of the
     * existing value, with an intervening newline character.
     *
     * @param name   the name of the parameter
     * @param value1 the first value to associate with name
     * @param value2 the second value to associate with name
     * @param value3 the third value to associate with name
     */
    public void appendParameter(String name, String value1, String value2, String value3) {
        appendParameter(name, (value1 + " " + value2 + " " + value3));
    }

    /**
     * Add a parameter (specified as a name-value1 value2 value3 value4 pair) to the ParInfo object. Parameter names are
     * not limited to just one "word" long values - they can be associated with a "many word" value. For example, say a
     * user wants to add have a parameter named <code>misc</code> in the ParInfo object, and wants <code>misc</code>'s
     * value to be <code>miscName
     * miscType miscInfo miscDefinition</code>, then call this function as follows: <BR>
     * <code>appendParameter("misc", "miscName", "miscType", "miscInfo",
     * "miscDefinition"); </code><BR>
     * If the ParInfo object already contains a parameter with that name, the new value is appended onto the end of the
     * existing value, with an intervening newline character.
     *
     * @param name   the name of the parameter
     * @param value1 the first value to associate with name
     * @param value2 the second value to associate with name
     * @param value3 the third value to associate with name
     * @param value4 the fourth value to associate with name
     */
    public void appendParameter(String name, String value1, String value2, String value3, String value4) {
        appendParameter(name, (value1 + " " + value2 + " " + value3 + " " + value4));
    }

    /**
     * Add a parameter (specified as a name-value1 value2 value3 value4 value5 pair) to the ParInfo object. Parameter
     * names are not limited to just one "word" long values - they can be associated with a "many word" value. For
     * example, say a user wants to add have a parameter named <code>misc</code> in the ParInfo object, and wants
     * <code>misc</code>'s value to be <code>
     * miscName miscType miscInfo miscDefinition miscMisc</code>, then call this
     * function as follows: <BR>
     * <code>appendParameter("misc", "miscName", "miscType", "miscInfo",
     * "miscDefinition", "miscMisc"); </code><BR>
     * If the ParInfo object already contains a parameter with that name, the new value is appended onto the end of the
     * existing value, with an intervening newline character.
     *
     * @param name   the name of the parameter
     * @param value1 the first value to associate with name
     * @param value2 the second value to associate with name
     * @param value3 the third value to associate with name
     * @param value4 the fourth value to associate with name
     * @param value5 the fifth value to associate with name
     */
    public void appendParameter(String name, String value1, String value2, String value3, String value4, String value5) {
        appendParameter(name, (value1 + " " + value2 + " " + value3 + " " + value4 + " " + value5));
    }

    /**
     * Add one or more parameter -> value pairs to this configInfo container.
     *
     * @param parameters a String containing a number of name = value pairs, each separated by an end-of-line character
     *                   (DBDefines.EOLN).
     */
    public void addParameters(String parameters) {
        String name, value;
        int delim, i = 0;

        // Split out all of the parameters delimited by newlines.
        // String[] ss = parameters.trim().split(DBDefines.EOLN);
        String[] ss = DBDefines.splitOnNewLine(parameters.trim());

        // Get all of the actual parameters out without their comments.
        ArrayList<String> lines = new ArrayList<String>(ss.length);
        for (i = 0; i < ss.length; i++)
            if (!ss[i].trim().startsWith(commentChar))
                lines.add(ss[i].trim());
        i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);

            // Handle comments on the end of the line.
            int pos = line.indexOf(commentChar);
            if (pos >= 0)
                line = line.substring(0, pos);

            delim = line.indexOf(delimeter);
            // Only process lines that do not start with a comment character, that
            // contain the delimeter, and have at least one non-space character
            // before the delimeter.
            if (delim > 0) {
                // split the line into a name and value.
                name = line.substring(0, delim).trim();
                value = line.substring(delim + 1).trim();

                // For lines that end with delimeter (ie., value is blank)
                // create a multiline value. Keep processing lines and adding
                // them to value until a blank line (or no more lines) is
                // encountered.
                if (value.length() == 0 && i < lines.size() - 1) {
                    String nextLine = lines.get(++i).trim();
                    // Handle comments on the end of the line.
                    pos = nextLine.indexOf(commentChar);
                    if (pos >= 0)
                        nextLine = nextLine.substring(0, pos);

                    if (nextLine.length() > 0)
                        value += nextLine;

                    while (i < lines.size() - 1 && nextLine.length() > 0) {
                        nextLine = lines.get(++i).trim();
                        // Handle comments on the end of the line.
                        pos = nextLine.indexOf(commentChar);
                        if (pos >= 0)
                            nextLine = nextLine.substring(0, pos);
                        if (nextLine.length() > 0)
                            value += DBDefines.EOLN + nextLine;
                    }
                }
                addParameter(name, value);
            }
            ++i;
        }
    }

    /**
     * Retrieve the value associated with a particular parameter name from this configInfo container. If the parameter
     * name does not exist in this configInfo container, then the name->default value association is added to the
     * ParInfo object and the specified default value is returned (future requests for the same parameter will return
     * this default value).
     *
     * @param name         the name of the parameter whose value is requested
     * @param defaultValue the value that should be returned if name is not a member of this configInfo container
     * @return either the desired parameter value, or the default value
     */
    public String getItem(String name, String defaultValue) {
        String plain = reduceParName(name);

        if (parameters.containsKey(plain))
            return parameters.get(plain);

        addParameterNoEmpty(name, defaultValue);

        return defaultValue;
    }

    /**
     * Retrieve the value associated with a particular parameter name from this configInfo container. If the parameter
     * name does not exist in this configInfo container, then default values that this ParInfo object retrieved from the
     * system are searched for suitable default values. If there is a default parameter name that ends with the
     * requested parameter name, then the system default value is returned. Currently, ParInfo has default values for
     * the following parameters: <br>
     * Username <br>
     * Password <br>
     * Instance <br>
     * Driver <br>
     * TableDefinitionTable <br>
     * DAOType <br>
     * Table Tablespace <br>
     * Index Tablespace
     * <p>
     * For example, if this method is called with parameter SourceUserName, and no value has been associated with that
     * parameter name, then the value of the system parameter 'username' will be returned.
     *
     * @param name the name of the parameter whose value is requested.
     * @return either the desired parameter value, or the default value.
     */
    public String getItem(String name) {
        if (name != null) {

            String plain = reduceParName(name);

            if (parameters.containsKey(plain))
                return parameters.get(plain);

            for (String defName : defaults.keySet()) {
                if (plain.endsWith(defName)) {
                    String defValue = defaults.get(defName);
                    addParameter(name, defValue);
                    return defValue;
                }
            }
        }
        return null;
    }

    /**
     * Return a String representation of the entire contents of the ParInfo container. For parameters that are equal to
     * empty strings or null, the parameters will be output as follows: parameter1_name = parameter2_name = some_value
     * Thus, this String is not suitable for being used to recreate a ParInfo object since when the ParInfo object is
     * reading in the above lines, it will treat parameter1_name as a multilined parameter (since the equals sign was
     * not followed by a value), and a getItem call with parameter1_name as the parameter will return the string
     * "parameter2_name = some_value". Any parameter containing ths substring 'password' is replaced with '*'
     * characters.
     *
     * @return a String representation of the entire contents of the ParInfo container.
     */
    @Override
    public String toString() {
        return getConfiguration();
    }

    /**
     * Return a String representation of the entire contents of the ParInfo container. For parameters that are equal to
     * empty strings or null, the parameters will be output as follows: parameter1_name = parameter2_name = some_value
     * Thus, this String is not suitable for being used to recreate a ParInfo object since when the ParInfo object is
     * reading in the above lines, it will treat parameter1_name as a multilined parameter (since the equals sign was
     * not followed by a value), and a getItem call with parameter1_name as the parameter will return the string
     * "parameter2_name = some_value". Any parameter containing ths substring 'password' is replaced with '*'
     * characters.
     *
     * @return a String representation of the entire contents of the ParInfo container
     */
    public String getConfiguration() {
        return getConfiguration(false);
    }

    /**
     * Return a String representation of the entire contents of the ParInfo container.
     *
     * @param printPassword boolean if false, any parameter containing the substring 'password' is replaced with '*'
     *                      characters.
     * @return a String representation of the entire contents of the ParInfo container
     */
    public String getConfiguration(boolean printPassword) {
        StringBuilder s = new StringBuilder();

        // The return String uses the original names ...
        for (String name : originalName.keySet()) {
            String plain = originalName.get(name);
            String par = parameters.get(plain);

            // Replace password with ****.
            if (par != null && !printPassword && plain.indexOf("password") >= 0) {
                char[] ss = par.toCharArray();
                for (int j = 0; j < ss.length; j++)
                    ss[j] = '*';
                par = new String(ss);
            }

            if (par != null && par.indexOf(DBDefines.EOLN) >= 0)
                par = DBDefines.EOLN + par + DBDefines.EOLN;

            s.append(name + " " + delimeter + " " + par + DBDefines.EOLN);
        }

        return s.toString();
    }

    public String filterSystemProperties(String parValue) {
        int i = parValue.indexOf("{$");
        while (i >= 0) {
            int j = parValue.indexOf("}");
            if (j <= i)
                return parValue;
            String sub = parValue.substring(i + 2, j - i);
            String sysProp = System.getProperty(sub);
            if (sysProp != null && sysProp.length() > 0) {
                if (i == 0)
                    parValue = sysProp + parValue.substring(j + 1);
                else
                    parValue = parValue.substring(0, i - 1) + sysProp + parValue.substring(j + 1);
            } else
                return parValue;
            i = parValue.indexOf("{$");
        }
        return parValue;
    }

    /**
     * Set filter for this ParInfo object. Lines in the parameter file that gets read into this ParInfo object that do
     * not start with this filter are ignored.
     *
     * @param filter filter to be applied to parameters (case insensitive)
     */
    public void setFilter(String filter) {
        if (filter != null)
            this.filter = filter.trim();
        else
            this.filter = null;
    }

    public String getParFileName() {
        return parFileName;
    }

    /**
     * This method deals with parameter elements that has spaces in it when the parameter element is surrounded by
     * quotes. This method then returns an array containing the individual elements of the parameter information;
     * parameter information in quotes constitutes one element. Typically, parameter information is separated by spaces,
     * but this method allows a parameter element to have spaces in it as long as it is surrounded by quotes.
     *
     * @param line line of parameter information information
     * @return array containing the individual elements of the parameter information; parameter information in quotes
     * constitutes one element. For example, if line is<br>
     * <code>origin_table_name origin</code><br>
     * this method returns a String array that looks like:<br>
     * <code>{origin_table_name, origin}</code><br>
     * If line is<br>
     * <code>"origin table name" origin</code><br>
     * this method returns a String array that looks like:<br>
     * <code>{origin table name, origin}</code><br>
     */
    public static String[] handleParametersWithSpaces(String line) {
        String[] words = line.split(" ");

        // If nothing in the parameter information has quotes in it, we are done.
        if (!line.contains("\""))
            return words;

        // Keep track of the items that will comprise the returned String array
        ArrayList<String> tempString = new ArrayList<String>();
        // Use this to "grow" parameter elements with
        StringBuilder growingString = new StringBuilder();

        // Take advantage of the split that has already been done instead of parsing each letter
        for (String str : words) {
            // Found an opening quote - start growing the information between the quotes. Add back the " " that was
            // there originally before the split
            if (str.startsWith("\""))
                growingString.append(str.substring(1) + " ");
                // Have already started growing a string - check if it is ending or if there is more between the quotes
            else if (growingString.length() > 0) {
                if (str.endsWith("\"")) {
                    growingString.append(str.substring(0, str.length() - 1));
                    tempString.add(growingString.toString());
                    growingString.setLength(0);
                } else
                    growingString.append(str + " ");
            }
            // No quote involved -- easy processing
            else
                tempString.add(str);
        }
        return tempString.toArray(new String[]{});
    }

    // Constants to try to avoid bugs and confusion that arise from misspellings
    public final static String DAO_TYPE = "DAOType";

    public final static String USERNAME = "Username";

    public final static String PASSWORD = "Password";

    public final static String INSTANCE = "Instance";

    public final static String DRIVER = "Driver";

    public final static String AUTO_COMMIT = "AutoCommit";

    public final static String AUTO_TABLE_CREATION = "AutoTableCreation";

    public final static String PROMPT_BEFORE_TRUNCATE = "PromptBeforeTruncate";

    public final static String TRUNCATE_TABLES = "TruncateTables";

    public final static String FIX_FOREIGN_KEYS = "FixForeignKeys";

    public final static String TOP_LEVEL_TABLE = "TopLevelTable";

    public final static String IDGAPS_TABLE = "IdGapsTable";

    public final static String REMAP_TABLE = "RemapTable";

    public final static String REMAP_SOURCE = "RemapSource";

    public final static String REMAP_LDDATE = "RemapLddate";

    public final static String RANKING_TABLE = "RankingTable";

    public final static String TABLE_DEFINITION_TABLE = "TableDefinitionTable";

    public final static String INDEX_TABLESPACE = "IndexTablespace";

    public final static String TABLE_TABLESPACE = "TableTablespace";

    public final static String DATE_FORMAT = "DateFormat";

    public final static String XML_INPUT_FILE = "XMLInputFile";

    public final static String XML_OUTPUT_FILE = "XMLOutputFile";

    public final static String QUERY = "Query";

    public final static String TABLES = "Tables";

    public final static String RELATIONSHIPS = "Relationships";

    public final static String APPLICATION = "Application";

    public final static String STATUS_LOG_FILE = "StatusLogFile";

    public final static String ERROR_LOG_FILE = "ErrorLogFile";

    public final static String WARNING_LOG_FILE = "WarningLogFile";

    public final static String ALTERNATE_CONFIG_FILE = "AlternateConfigFile";

    public final static String RECORD_ROW_HISTORIES = "RecordRowHistories";

    public final static String NEXT_ID_SEQUENCES = "NextIDSequences";

    public final static String USE_TABLE_TYPES = "UseTableTypes";

    public final static String IDGAPS_TABLE_USE_TABLE = "IdGapsTableUseTable";

    public final static String REMAP_TABLE_USE_TABLE = "RemapTableUseTable";

    public final static String RANKING_TABLE_USE_TABLE = "RankingTableUseTable";

    public final static String CONSOLE_OUTPUT = "ConsoleOutput";

    // public final static String CONFIG_NAME = ".name";
    //    
    // public final static String CONFIG_TYPE = ".type";
    //    
    // public final static String CONFIG_SQL_DRIVER = ".sql.driver";
    //    
    // public final static String CONFIG_SQL_INSTANCE = ".sql.instance";
    //    
    // public final static String CONFIG_SQL_USERNAME = ".sql.username";
    //    
    // public final static String CONFIG_SQL_PASSWORD = ".sql.password";
    //    
    // public final static String CONFIG_SQL_TABLE_DEFINITION_TABLE = ".sql.tabledefinitiontable";
    //    
    // public final static String CONFIG_FLATFILE_PATH = ".flatfile.path";

}
