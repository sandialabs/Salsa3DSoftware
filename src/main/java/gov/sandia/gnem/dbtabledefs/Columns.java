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
package gov.sandia.gnem.dbtabledefs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This class supports the definition of all columns used by any table extending BaseRowFile. It
 * contains an enum of public FieldType (STRING, DOUBLE, LONG), of which others can be added, and a
 * ColumnDescription object for each column containing the column name, FieldType, a boolean flag
 * denoting if the column is absolutely required (true) or can be omitted in input or output tables
 * (false), and an output format descriptor. The column information is stored in an
 * ArrayList<String> of ordered column names (columnOrderedNames) and a map of column names
 * associated with its ColumnDescription object (columnNameMap). Having both allows for fast
 * searching for either type of lookup (list/map).
 * 
 * Various getters provide a HashSet of all column names, a HashSet of all required column names,
 * the number of columns, a HashSet of all columns of a common type, the input type of a column by
 * name, the required flag of a column by name, a String array of all column names, the validity
 * (true/false) of an input column name, the required validity (true/false) of an input column name,
 * a method to add new column names to the Columns object, two validity methods that check 1) if an
 * entire input array of names are valid and contained in the Columns object; and 2) if an entire
 * input array of names are valid and contained in the required set of columns in the Columns
 * object, and finally, and input setter that given the column name, the BaseRowFile reference
 * containing the field to be set, and a new string value for the field; will call the appropriate
 * type specific setter to set the field.
 * 
 * @author jrhipp
 *
 */
public class Columns {
  /**
   * The supported list of table field types.
   *
   */
  public enum FieldType {
    STRING, DOUBLE, LONG;
  }

  /**
   * The column descriptor object containing fields for the the column name, FieldType, required
   * flag, and output format string.
   *
   */
  public class ColumnDescription {
    public String name;
    public FieldType type;
    public boolean required;
    public String outputFormat;

    public ColumnDescription(String name, FieldType type, boolean required, String format) {
      this.name = name;
      this.type = type;
      this.required = required;
      this.outputFormat = format;
    }
  }

  /**
   * The ordered list containing all column names for this Columns object.
   */
  private ArrayList<String> columnOrderedNames = new ArrayList<String>();

  /**
   * The map of column names associated with their ColumnDescription object.
   */
  private HashMap<String, ColumnDescription> columnNameMap =
      new HashMap<String, ColumnDescription>();

  /**
   * The set of all column names in this Columns object.
   */
  private HashSet<String> columnNameSet = null;

  /**
   * The set of all required column names in this Columns object.
   */
  private HashSet<String> requiredColumnNameSet = null;

  /**
   * Default constructor.
   */
  public Columns() {}

  /**
   * Returns the number of columns in this Columns object.
   * 
   * @return The number of columns in this Columns object.
   */
  public int size() {
    return columnNameMap.size();
  }

  /**
   * Adds a new column to this Columns object.
   * 
   * @param name The name of the new column.
   * @param type The FieldType of the new column.
   * @param required The required flag of the new column.
   */
  public void add(String name, FieldType type, String format) {
    columnOrderedNames.add(name.toLowerCase());
    columnNameMap.put(name.toLowerCase(),
        new ColumnDescription(name.toLowerCase(), type, true, format));
  }

  /**
   * Sets all "required" fields for columns in the input array names to the input requiredStatus
   * (true = required, false = optional).
   * 
   * @param names The array of columns whose required status is to be set.
   * @param requiredStatus The new required status for the input columns.
   * @throws IOException
   */
  public void setColumnRequiredStatus(String[] names, boolean requiredStatus) throws IOException {
    for (int i = 0; i < names.length; ++i) {
      setColumnRequiredStatus(names[i], requiredStatus);
    }
  }

  /**
   * Sets the "required" field for the input column name to the input requiredStatus (true =
   * required, false = optional).
   * 
   * @param names The column name required status is to be set.
   * @param requiredStatus The new required status for the input column.
   * @throws IOException
   */
  public void setColumnRequiredStatus(String name, boolean requiredStatus) throws IOException {
    ColumnDescription cd = columnNameMap.get(name.toLowerCase());
    if (cd == null) {
      throw new IOException("Error: input column name: " + name + " is not defined ...");
    }
    cd.required = requiredStatus;
    requiredColumnNameSet = null;
  }

  /**
   * Returns the FieldType for the input column name. If name is not a column name then null is
   * returned.
   * 
   * @param name The input name of the column whose FieldType will be returned.
   * @return The FieldType of the input column name or null if the name is not a column name of this
   *         object.
   */
  public FieldType getColumnNameFieldType(String name) {
    ColumnDescription cd = columnNameMap.get(name.toLowerCase());
    if (cd == null)
      return null;
    else
      return cd.type;
  }

  /**
   * Returns the output format specification for the input column name. If name is not a column name
   * then null is returned.
   * 
   * @param name The input name of the column whose format specifier will be returned.
   * @return The output format specification of the input column name or null if the name is not a
   *         column name of this object.
   */
  public String getColumnNameFormatSpecification(String name) {
    ColumnDescription cd = columnNameMap.get(name.toLowerCase());
    if (cd == null)
      return null;
    else
      return cd.outputFormat;
  }

  /**
   * Returns true if the input name is a column that is required, otherwise false is returned.
   * 
   * @param name The input name of the column whose required setting will be returned.
   * @return True if the input name is a column that is required, otherwise false is returned.
   */
  public boolean getColumnNameRequired(String name) {
    ColumnDescription cd = columnNameMap.get(name.toLowerCase());
    if (cd == null)
      return false;
    else
      return cd.required;
  }

  /**
   * Returns the ordered list of column names as an array.
   * 
   * @return The ordered list of column names as an array.
   */
  public String[] getColumnNames() {
    String[] names = new String[columnOrderedNames.size()];
    return columnOrderedNames.toArray(names);
  }

  /**
   * Returns the set of all column names.
   * 
   * @return The set of all column names.
   */
  public HashSet<String> getColumnNameSet() {
    if (columnNameSet == null)
      buildColumnNameSet();
    return columnNameSet;
  }

  /**
   * Returns true if the input name is a valid column name of this object.
   * 
   * @param name The input name to be validated.
   * @return True if the input name is a valid column name of this object.
   */
  public boolean isValidColumnName(String name) {
    if (columnNameSet == null)
      buildColumnNameSet();
    if (columnNameSet.contains(name.toLowerCase()))
      return true;
    else
      return false;
  }

  /**
   * Builds the column name set.
   */
  private void buildColumnNameSet() {
    columnNameSet = new HashSet<String>();
    for (Map.Entry<String, ColumnDescription> entry : columnNameMap.entrySet())
      columnNameSet.add(entry.getKey());
  }

  /**
   * Returns the set of all required column names.
   * 
   * @return The set of all required column names.
   */
  public HashSet<String> getRequiredColumnNameSet() {
    if (requiredColumnNameSet == null)
      buildRequiredColumnNameSet();
    return requiredColumnNameSet;
  }

  /**
   * Returns true if the input name is a valid required column name.
   * 
   * @param name The name to be validated.
   * @return True if the input name is a valid required column name.
   */
  public boolean isValidRequiredColumnName(String name) {
    if (requiredColumnNameSet == null)
      buildRequiredColumnNameSet();
    if (requiredColumnNameSet.contains(name.toLowerCase()))
      return true;
    else
      return false;
  }

  /**
   * Builds the required column name set.
   */
  private void buildRequiredColumnNameSet() {
    requiredColumnNameSet = new HashSet<String>();
    for (Map.Entry<String, ColumnDescription> entry : columnNameMap.entrySet())
      if (entry.getValue().required)
        requiredColumnNameSet.add(entry.getKey());
  }

  /**
   * Returns a set of all column names of a particular FieldType.
   * 
   * @param type The FieldType for which a set of column names is returned.
   * @return A set of all column names of a particular FieldType.
   */
  public HashSet<String> getTypeColumnNameSet(FieldType type) {
    HashSet<String> columnNameSet = new HashSet<String>();
    for (Map.Entry<String, ColumnDescription> entry : columnNameMap.entrySet())
      if (entry.getValue().type == type)
        columnNameSet.add(entry.getKey());

    return columnNameSet;
  }

  /**
   * Validates all names in the input array for containment in this Columns objects column
   * definitions. If any are not valid an error is thrown for the invalid name. This method does not
   * change state.
   * 
   * @param names The input array to be validated.
   * @throws IOException
   */
  public void containsAllRequiredColumns(String[] names) throws IOException {
    HashSet<String> requiredNames = new HashSet<String>();
    for (int i = 0; i < names.length; ++i) {
      if (isValidRequiredColumnName(names[i]))
        requiredNames.add(names[i].toLowerCase());
    }

    if (requiredNames.size() != requiredColumnNameSet.size()) {
      throw new IOException("Error: input column names: " + Arrays.toString(names) + "\n"
          + "does not contain all required column names: " + requiredColumnNameSet.toString());
    }
  }

  /**
   * Validates all names in the input array for containment in this Columns objects required column
   * definitions. If any are not valid an error is thrown for the invalid name. This method does not
   * change state.
   * 
   * @param names The input array to be validated.
   * @throws IOException
   */
  public void containsValidColumnNames(String[] names) throws IOException {
    for (int i = 0; i < names.length; ++i) {
      if (!isValidColumnName(names[i])) {
        throw new IOException(
            "Error: input column name \"" + names[i] + "\" is not a valid name ...");
      }
    }
  }
}
