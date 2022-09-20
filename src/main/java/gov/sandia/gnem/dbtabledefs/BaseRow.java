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

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Pattern;

import gov.sandia.gnem.dbtabledefs.Columns.FieldType;

public abstract class BaseRow implements Cloneable, Serializable {
  private static final long serialVersionUID = -5502275914340579627L;

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * A reference to an unspecified Object that is attached to each instance of a BaseRow object.
   * BaseRow and derived classes do nothing with this reference. Applications that use these classes
   * are free to attach anything they want to a BaseRow object. For example, an Arrival object could
   * specify a reference to a Site object, or an Origin object might reference an ArrayList<Assoc>,
   * etc..
   */
  public Object attachment;

  /**
   * Used by scanner to parse all strings and counting those strings enclosed by paired double or
   * single quotes as a single token. Paired quotes are automatically removed. See method
   * scannerRemoveQuotes(Scanner input) below.
   */
  private static String rxPat = "\"[^\"]*\"" + "|'[^']*'" + "|[A-Za-z0-9+-:;_()']+";
  private static Pattern rx = Pattern.compile(rxPat);

  protected static String tokenDelim = "\t";

  /**
   * DateFormat that will format dates using the GMT time zone and the date format specified for
   * this schema (yy/MM/dd HH:mm:ss).
   */
  static public DateFormat dateFormatGMT = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

  static {
    dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
  };

  static public String createTableAddendum = "";

  /**
   * MD5 hash of the contents of this. Used for testing equality and for generating hashCode()
   */
  private BigInteger hash;

  /**
   * MD5 hash of the contents of this. Used for testing equality and for generating hashCode()
   */
  public BigInteger getHash() {
    if (hash == null) {
      try {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        ByteBuffer byteBuffer = ByteBuffer.allocate(maxBytes());
        write(byteBuffer);
        for (byte b : byteBuffer.array())
          messageDigest.update(b);
        hash = new BigInteger(messageDigest.digest());
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return hash;
  }

  public void setHash(BigInteger hash) {
    this.hash = hash;
  }

  public void incrementHash(BigInteger hash) {
    this.hash.add(hash);
  }

  @Override
  public int hashCode() {
    return getHash().hashCode();
  }

  public boolean isHashNull() {
    return hash == null;
  }

  /**
   * Equal operator.
   */
  @Override
  public boolean equals(Object other) {
    return other instanceof BaseRow && this.getHash().equals(((BaseRow) other).getHash());
  }

  /**
   * Retrieve a String representation of a sql statement that can be used to insert the values of
   * this BaseRow object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this BaseRow object into a database.
   */
  abstract public String getInsertSql(String tableName);

  /**
   * Write this row to an Object[] array.
   */
  abstract public Object[] getValues();

  /**
   * Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  abstract public Object[] getValues(java.sql.Date lddate);

  /**
   * Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  abstract public Object[] getValues(java.util.Date lddate);

  /**
   * Write the contents of this object to a binary file.
   * 
   * @param output
   * @throws IOException
   */
  abstract public void write(DataOutputStream output) throws IOException;

  /**
   * Write the contents of this object to a ByteBuffer.
   * 
   * @param output
   */
  abstract public void write(ByteBuffer output);

  /**
   * Write the contents of this object to an ascii file.
   * 
   * @param output
   * @throws IOException
   */
  abstract public void write(BufferedWriter output) throws IOException;

  /**
   * Maximum number of bytes required to store an instance of this in a ByteBuffer or
   * DataOutputStream. Actual number will be less if the object contains any Strings and those
   * Strings have less than maximum length.
   * 
   * @return maximum number of bytes required to store a binary instance of this.
   */
  abstract public int maxBytes();

  /**
   * Read a String from a binary file. First reads the integer length of String then the actual
   * String contents.
   * 
   * @param input
   * @return the String
   */
  static public String readString(DataInputStream input) throws IOException {
    int size = input.readInt();
    if (size == 0)
      return "";
    byte[] buf = new byte[size];
    input.read(buf);
    return new String(buf);
  }

  /**
   * Write integer length of String, followed by String contents to a binary file.
   * 
   * @param output
   * @param s
   * @throws IOException
   */
  static public void writeString(DataOutputStream output, String s) throws IOException {
    if (s == null || s.isEmpty())
      output.writeInt(0);
    else {
      output.writeInt(s.length());
      output.writeBytes(s);
    }
  }

  /**
   * Read a String from a ByteBuffer. First reads the integer length of String then the actual
   * String contents.
   * 
   * @param input
   * @return the String
   */
  public static String readString(ByteBuffer input) {
    int size = input.getInt();
    if (size == 0)
      return "";
    byte[] buf = new byte[size];
    input.get(buf);
    return new String(buf);
  }

  /**
   * Write integer length of String, followed by String contents to a ByteBuffer.
   * 
   * @param output
   * @param s
   * @throws IOException
   */
  public static void writeString(ByteBuffer output, String s) {
    if (s == null || s.isEmpty())
      output.putInt(0);
    else {
      output.putInt(s.length());
      output.put(s.getBytes());
    }
  }

  /**
   * Return true if primary keys are equal in this and other. Returns false if primary keys are not
   * defined.
   * 
   * @param other
   * @return true if primary keys are equal in this and other.
   */
  public boolean equalPrimaryKey(BaseRow other) {
    return false;
  }

  /**
   * Return true if unique keys are equal in this and other. Returns false if unique keys are not
   * defined.
   * 
   * @param other
   * @return true if unique keys are equal in this and other.
   */
  public boolean equalUniqueKey(BaseRow other) {
    return false;
  }

  /**
   * If the next token has any single or double quotes then the entire quoted string is returned as
   * a token and the quotes are removed.
   * 
   * @param input The scanner whose next token will be checked for enclosing quotes.
   * @return The next scanner token with no quotes if any were originally contained.
   */
  public static String scannerRemoveQuotes(Scanner input) {
    String t = input.findInLine(rx);
    if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
      return t.substring(1, t.length() - 1).trim();
    } else
      return t.trim();
  }

  public static String getTokenDelimiter() {
    return tokenDelim;
  }

  /**
   * Assembles the token delimiter from the input value in tokenDelimiter. The input value is a
   * space delimited string that uses "tab" for "\t", "comma" for ",", and "space" for " ". Any
   * other character can be represented also.
   * 
   * For example, if the desired delimiter is ",\t *" then the input string would be "comma tab
   * space *".
   * 
   * Using the long names for whitespace characters was necessary since the input tokenDelimiter is
   * read from a properties file.
   * 
   * @param tokenDelimiter input tokenDelimiter that will be reassembled to remove any whitespace
   *        names and replace them with their actual character.
   */
  public static void setTokenDelimiter(String tokenDelimiter) {
    String[] tokens = tokenDelimiter.split(" ");
    tokenDelim = "";
    for (int i = 0; i < tokens.length; ++i) {
      if (tokens[i].toLowerCase().equals("tab"))
        tokenDelim += "\t";
      else if (tokens[i].toLowerCase().equals("comma"))
        tokenDelim += ",";
      else if (tokens[i].toLowerCase().equals("space"))
        tokenDelim += " ";
      else if (!tokens[i].equals(""))
        tokenDelim += tokens[i];
    }
    if (tokenDelim.equals(""))
      tokenDelim = "\t";
  }

  /**
   * Parses the input Scanner (assumed to be a single line of tokens) into it's separate String
   * tokens using tokenDelim as the token separator. An error is thrown if there are fewer tokens
   * than the input expected amount. If more tokens are present, the returned array is truncated and
   * only contains the number of expected tokens. The extra tokens are ignored.
   * 
   * @param input The input Scanner (assumed to be a single line of tokens).
   * @param className The calling class used for error information.
   * @param expected The expected number of tokens.
   * @return The list of scanned tokens (strings) exactly equal to "expected".
   * @throws IOException
   */
  public static String[] getLineTokens(Scanner input, String className, int expected)
      throws IOException {
    // create a list to store the tokens and set the scanners token delimiter.
    List<String> tokenList = new ArrayList<String>();
    input.useDelimiter(tokenDelim);

    if (tokenDelim.equals(" "))
    {
      // loop over the scanner retrieving tokens
      while (input.hasNext()) {
        // get the next token. If empty or a single comma skip the token. Otherwise,
        // add it to the token list.
        String t = scannerRemoveQuotes(input).trim();
        if (!t.equals("") && !t.equals(","))
          tokenList.add(t);
      }
    }
    else
    {
      // loop over the scanner retrieving tokens
      while (input.hasNext()) {
        // get the next token. If empty or a single comma skip the token. Otherwise,
        // add it to the token list.
        String t = input.next().trim();
        if (!t.equals("") && !t.equals(","))
          tokenList.add(t);
      }
    }

    // Throw an error if fewer than the expected number of tokens was found. If
    // more were found than "expected" then truncate the list so exactly
    // "expected" tokens are returned.
    if (tokenList.size() < expected) {
      throw new IOException(String.format(
          "Error parsing line tokens for class %s.  " + "Expected %d tokens but found %d.",
          className, expected, tokenList.size()));
    } else if (tokenList.size() > expected)
      tokenList = tokenList.subList(0, expected);

    // return the tokens list as an array
    return (String[]) tokenList.toArray(new String[tokenList.size()]);
  }

  public static long getInputLong(String input, String parameterName, String className)
      throws IOException {
    try {
      return Long.valueOf(input);
    } catch (Exception ex) {
      throw new IOException(getErrorMessage(input, parameterName, className, ex.getMessage()));
    }
  }

  public static double getInputDouble(String input, String parameterName, String className)
      throws IOException {
    try {
      return Double.valueOf(input);
    } catch (Exception ex) {
      throw new IOException(getErrorMessage(input, parameterName, className, ex.getMessage()));
    }
  }

  /**
   * Trims string and removes " or ' from beginning and end of string if present. If the input
   * string doesn't need trimming and is not quoted the input string is simply returned.
   * 
   * @param input String to be trimmed and stripped of " or '.
   * @return String without beginning and ending " or '.
   */
  public static String getInputString(String input) {
    String s = input.trim();
    if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
      return s.substring(1, s.length() - 1);
    }

    return s;
  }

  /**
   * Returns a quoted input string if tokenDelim is a space (for output).
   * 
   * @param input String to be quoted if tokenDelim is a space.
   * @return Quoted string
   */
  public static String quoteSpcDelimString(String input) {
    if (tokenDelim.equals(" "))
      return "\"" + input.trim() + "\"";
    else
      return input;
  }

  private static String getErrorMessage(String input, String parameterName, String className,
      String errMssg) {
    return "Error Assigning parmeter \"" + parameterName + " = " + input + "\" to Class \""
        + className + "\"\n" + errMssg;
  }

  /**
   * Abstract method defined by derived classes for returning a String field for the given column
   * name.
   * 
   * @param name The String field to be returned.
   * @return A String field for the given column name.
   * @throws IOException
   */
  abstract public String getStringField(String name) throws IOException;

  /**
   * Abstract method defined by derived classes for returning a Double field for the given column
   * name.
   * 
   * @param name The Double field to be returned.
   * @return A Double field for the given column name.
   * @throws IOException
   */
  abstract public double getDoubleField(String name) throws IOException;

  /**
   * Abstract method defined by derived classes for returning a Long field for the given column
   * name.
   * 
   * @param name The Long field to be returned.
   * @return A Long field for the given column name.
   * @throws IOException
   */
  abstract public long getLongField(String name) throws IOException;

  /**
   * Abstract method defined by derived classes for setting a String field (column) value to the
   * given input.
   * 
   * @param name The String field to be assigned.
   * @param input The input value to be assigned to the given field.
   * @throws IOException
   */
  abstract public void setStringField(String name, String input) throws IOException;

  /**
   * Abstract method defined by derived classes for setting a Double field (column) value to the
   * given input.
   * 
   * @param name The Double field to be assigned.
   * @param input The input value to be assigned to the given field.
   * @throws IOException
   */
  abstract public void setDoubleField(String name, String input) throws IOException;

  /**
   * Abstract method defined by derived classes for setting a Long field (column) value to the given
   * input.
   * 
   * @param name The Long field to be assigned.
   * @param input The input value to be assigned to the given field.
   * @throws IOException
   */
  abstract public void setLongField(String name, String input) throws IOException;

  /**
   * Called by a constructor of each derived object that extends this base class and supports
   * Scanner line input in the derived objects constructor argument list.
   * 
   * @param inputValues The string array of values taken from the input Scanner that will be set to
   *        the columns whose matching names are defined in the derived objects input column names
   *        list.
   * @param inputColumns The names of the input columns to be set. These are typically the static
   *        inputColumnNames of the high level dbTableDef objects (e.g. Arrival).
   * @param columns The column objects defined by a high level dbTableDef object (eg. Arrival).
   * @throws IOException
   */
  public void setInputValues(String[] inputValues, String[] inputColumns, Columns columns)
      throws IOException {
    if (inputValues.length != inputColumns.length) {
      throw new IOException("Error: The input column value array size (" + inputValues.length
          + ") is not equal to the defined input column name array size (" + inputColumns.length
          + ") ...");
    }

    for (int i = 0; i < inputColumns.length; ++i) {
      String name = inputColumns[i].toLowerCase();
      FieldType type = columns.getColumnNameFieldType(name);
      if (type == null)
        throw new IOException("Error: Field= \"" + name + "\" is not defined ...");

      switch (type) {
        case STRING:
          setStringField(name.toLowerCase(), inputValues[i]);
          break;
        case DOUBLE:
          setDoubleField(name.toLowerCase(), inputValues[i]);
          break;
        case LONG:
          setLongField(name.toLowerCase(), inputValues[i]);
          break;
        default:
          throw new IOException("Error: Unknow input type: \"" + type.name() + "\" ...");
      }
    }
  }

  /**
   * Used to form a formatted output string of all of the tables fields specified in the input
   * column names list of this table. The various fields are separated by tokenDelim specified in
   * BaseRow.
   *
   * @param outputColumns The names of the output columns to be added to the returned formatted
   *        string. These are typically the static outputColumnNames of the high level dbTableDef
   *        objects (e.g. Arrival).
   * @param columns The column objects defined by a high level dbTableDef object (eg. Arrival).
   * @return A formatted output string of all of the tables fields specified in the
   *         outputColumnNames list of this table.
   * @throws IOException
   */
  public String getOutputString(String[] outputColumnNames, Columns columns) throws IOException {
    String outputString = "";
    for (int i = 0; i < outputColumnNames.length; ++i) {
      String name = outputColumnNames[i].toLowerCase();

      FieldType type = columns.getColumnNameFieldType(name);
      if (type == null)
        throw new IOException("Error: Field= \"" + name + "\" is not defined ...");

      String frmt = columns.getColumnNameFormatSpecification(name);
      switch (type) {
        case STRING:
          String valueS = getStringField(name);
          if (tokenDelim.equals(" ")) frmt = "\""+frmt+"\"";
          outputString += String.format(frmt, valueS);
          break;
        case DOUBLE:
          double valueD = getDoubleField(name);
          outputString += String.format(frmt, valueD);
          break;
        case LONG:
          long valueL = getLongField(name);
          outputString += String.format(frmt, valueL);
          break;
        default:
          throw new IOException("Error: Unknow input type: \"" + type.name() + "\" ...");
      }

      if (i < outputColumnNames.length - 1)
        outputString += tokenDelim;
    }

    return outputString;
  }

  /**
   * Used to form a formatted output header string of all of the tables fields specified in the
   * input column names list of this table. The various field names are separated by tokenDelim
   * specified in BaseRow.
   *
   * @return A formatted output header string of all of the tables fields specified in the input
   *         column names list of this table.
   * @throws IOException
   */
  public static String getOutputHeaderString(String[] outputColumnNames) {
    String outputString = "#";
    for (int i = 0; i < outputColumnNames.length; ++i) {
      outputString += outputColumnNames[i].toLowerCase();
      if (i < outputColumnNames.length - 1)
        outputString += tokenDelim;
    }

    return outputString;
  }
}
