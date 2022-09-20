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
package gov.sandia.gnem.dbutillib.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Parser {
    public Parser() {
    }

    public Parser(String input, Table targetTable)
            throws DBDefines.FatalDBUtilLibException {
        this(input, null, targetTable);
    }

    public Parser(String input, Table sourceTable, Table targetTable)
            throws DBDefines.FatalDBUtilLibException {
        input = input.trim();
        if (input.toLowerCase().equals("where"))
            input = "";
        else if (input.toLowerCase().startsWith("where "))
            input = input.substring(5).trim();

        if (input.length() == 0)
            alwaysTrue = true;
        else {
            lumpable = true;
            parseTree = new ParserNode(input, null, sourceTable, targetTable);
        }
    }

    public boolean isLumpable() {
        return lumpable;
    }

    public boolean evaluate(Row sourceRow, Row targetRow)
            throws DBDefines.FatalDBUtilLibException {
        return alwaysTrue || (Boolean) parseTree.evaluate(sourceRow, targetRow);
    }

    public boolean evaluate(Row targetRow)
            throws DBDefines.FatalDBUtilLibException {
        return evaluate(null, targetRow);
    }

    private boolean alwaysTrue = false;
    private ParserNode parseTree;
    private LinkedHashMap<String, String[]> operators;
    private HashSet<Character> digits;
    //private int nodeIdSource = 0;
    private boolean lumpable = false;

    /**
     * This inner class of Parser represents a single node in the parse tree.
     * A node consists of an operator (or, and, =, <=, +, -, *, /, etc) and
     * one or more operands.  Operands can be other ParserNodes, indexes into
     * the Object[] arrays of either the source or target Tables, or explicit
     * values (numbers or string).
     */
    class ParserNode {
        private ParserNode parent;
        private String operator = null;
        private ArrayList<Object> operands = new ArrayList<Object>();
        protected int type = DBDefines.UNKNOWN_TYPE;
        private int sourceIndex = -1;
        private int targetIndex = -1;

        //private int nodeId;
        //private String nodeText;

        private ParserNode(String input, ParserNode parent, Table sourceTable,
                           Table targetTable)
                throws DBDefines.FatalDBUtilLibException {

            if (operators == null) {
                // if this ParserNode is the root, then set up the operator
                // arrays.  This is a list of lists, each operator in the same
                // list has the same priority.  The lists are in oder of
                // decreasing priority.
                operators = new LinkedHashMap<String, String[]>();
                operators.put(" or ", new String[]
                        {" or "});
                operators.put(" and ", new String[]
                        {" and "});
                operators.put(" in ", new String[]
                        {" in "});
                operators.put(" between ", new String[]
                        {" between "});
                operators.put("comp", new String[]
                        {"<=", ">=", "!=", "=", "<", ">"});
                operators.put("op1", new String[]
                        {"*", "/"});
                operators.put("op2", new String[]
                        {"+", "-"});

                // initialize a set of characters that are chars that appear
                // in numbers.  This so we can check characters in a string
                // to determine if we are currently in a number.
                digits = new HashSet<Character>();
                digits.add('0');
                digits.add('1');
                digits.add('2');
                digits.add('3');
                digits.add('4');
                digits.add('5');
                digits.add('6');
                digits.add('7');
                digits.add('8');
                digits.add('9');
                digits.add('.');
                digits.add('+');
                digits.add('-');
                digits.add('e');
                digits.add('E');
            }

            this.parent = parent;
            int i = 0;

            //nodeId = nodeIdSource++;

            HashMap<String, ArrayList<Integer>>
                    index = new HashMap<String, ArrayList<Integer>>();
            int depth = 0, minDepth = Integer.MAX_VALUE;
            boolean literal = false, between = false, haveToken = false,
                    inNumber = true;
            char c = '\b'; // initialize backspace which cannot occur in where clause
            // do a single traverse of the input string and populate the index ArrayList
            // with the indeces of the operators.
            for (i = 0; i < input.length(); i++) {
                char last = c;
                c = input.charAt(i);

                if (c == '\'')
                    literal = !literal
                            ;
                if (!literal) {
                    if (c == '(') {
                        ++depth;
                    } else if (c == ')') {
                        --depth;
                        if (depth < 0)
                            throw new DBDefines.FatalDBUtilLibException(
                                    "ERROR in Parser:  Unmatched ')' in substring "
                                            + input);
                    } else {
                        // get the substring that starts at i.
                        String right = input.substring(i).toLowerCase();

                        if (digits.contains(c) && (last == ' ' || last == '('))
                            inNumber = true;
                        else if (!digits.contains(c))
                            inNumber = false;
                        if (inNumber && (c == '+' || c == '-') && last != 'e' &&
                                last != 'E')
                            inNumber = false;

                        if (c != ' ' && c != '(')
                            haveToken = true;

                        if (right.startsWith(" or ")) {
                            haveToken = false;
                            setIndex(index, " or ", depth, i);
                            minDepth = Math.min(minDepth, depth);
                            if (depth == 0)
                                break;
                        } else if (right.startsWith(" and ")) {
                            haveToken = false;
                            if (between)
                                between = false;
                            else {
                                setIndex(index, " and ", depth, i);
                                minDepth = Math.min(minDepth, depth);
                            }
                        } else if (right.startsWith(" between ")) {
                            haveToken = false;
                            setIndex(index, " between ", depth, i);
                            minDepth = Math.min(minDepth, depth);
                            between = true;
                        } else if (right.startsWith(" in ")) {
                            haveToken = false;
                            setIndex(index, " in ", depth, i);
                            minDepth = Math.min(minDepth, depth);
                        } else {
                            for (String op : operators.get("comp"))
                                if (right.startsWith(op)) {
                                    haveToken = false;
                                    setIndex(index, "comp", depth, i);
                                    minDepth = Math.min(minDepth, depth);
                                }

                            for (String op : operators.get("op1"))
                                if (right.startsWith(op)) {
                                    haveToken = false;
                                    setIndex(index, "op1", depth, i);
                                    minDepth = Math.min(minDepth, depth);
                                }

                            if (haveToken && !inNumber)
                                for (String op : operators.get("op2"))
                                    if (right.startsWith(op)) {
                                        haveToken = false;
                                        setIndex(index, "op2", depth, i);
                                        minDepth = Math.min(minDepth, depth);
                                    }

                        }
                    }
                } // end of if !literal section
            }

            if (literal)
                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Unmatched ' in substring "
                                + input);

            if (depth > 0)
                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Unmatched '(' in substring "
                                + input);

            if (depth < 0)
                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Unmatched ')' in substring "
                                + input);

            if (minDepth > 0 && minDepth < Integer.MAX_VALUE) {
                // there should be open and close parentheses at the begining and
                // end of input.  The number of them = minDepth.  Delete them.
                i = 0;
                StringBuilder temp = new StringBuilder(input);
                while (i < minDepth && temp.length() > 0) {
                    if (temp.charAt(0) == ' ')
                        temp.deleteCharAt(0);
                    else if (temp.charAt(0) == '(') {
                        temp.deleteCharAt(0);
                        ++i;
                    } else
                        throw new DBDefines.FatalDBUtilLibException(
                                "ERROR in Parser:  Expected '(' or ' ' but found '"
                                        + temp.charAt(0) + "' in string "
                                        + input);

                }
                int ndel = input.length() - temp.length();

                i = 0;
                while (i < minDepth && temp.length() > 0) {
                    if (temp.charAt(temp.length() - 1) == ' ')
                        temp.deleteCharAt(temp.length() - 1);
                    else if (temp.charAt(temp.length() - 1) == ')') {
                        temp.deleteCharAt(temp.length() - 1);
                        ++i;
                    } else
                        throw new DBDefines.FatalDBUtilLibException(
                                "ERROR in Parser:  Expected ')' or ' ' but found '"
                                        + temp.charAt(0) + "' in string "
                                        + input);
                }

                //System.out.println("2  "+temp.toString());

                // have to visit the indexes of all the operators in index and reduce
                // them by the number of characters that got deleted off the front
                // end of the input string.
                //for (String op : new String[] {" or ", " and ", " in ", " between ", "comp", "op1", "op2"})
                for (String op : operators.keySet()) {
                    ArrayList<Integer> idx = index.get(op);
                    if (idx != null)
                        for (int x = 0; x < idx.size(); x++)
                            if (idx.get(x) != null)
                                idx.set(x, idx.get(x) - ndel);
                }
                input = temp.toString();
                //System.out.println("3  "+temp.toString());
            }

            // check the input for operators.
            for (String op : operators.keySet()) {
                ArrayList<Integer> indeces = index.get(op);
                if (indeces != null && indeces.size() > minDepth &&
                        indeces.get(minDepth) != null) {
                    i = indeces.get(minDepth);

                    for (String ops : operators.get(op))
                        if (input.substring(i).toLowerCase().startsWith(ops)) {
                            operator = ops;
                            break;
                        }

                    if (!operator.equals("=")) lumpable = false;

                    if (operator.equals(" between ")) {
                        operands.add(new ParserNode(input.substring(0, i).trim(), this,
                                sourceTable, targetTable));

                        type = ((ParserNode) operands.get(0)).type;

                        ParserNode right = new ParserNode(input.substring(i +
                                operator.length()), this, sourceTable, targetTable);

                        if (right.operator.equals(" and ") &&
                                right.operands.size() == 2)
                            for (int k = 0; k < 2; k++) {
                                right.operator = operator;
                                operands.add(right.operands.get(k));
                            }
                        else
                            throw new DBDefines.FatalDBUtilLibException(
                                    "ERROR in Parser: Unable to parse 'between' clause: "
                                            + input);
                    } else if (operator.equals(" and ") && parent != null && parent.operator.equals(" between ")) {
                        type = parent.type;
                        operands.add(new ParserNode(input.substring(0, i).trim(), this,
                                sourceTable, targetTable));
                        operands.add(new ParserNode(input.substring(i +
                                operator.length()).trim(), this, sourceTable, targetTable));
                    } else {
                        operands.add(new ParserNode(input.substring(0, i).trim(), this,
                                sourceTable, targetTable));
                        type = ((ParserNode) operands.get(0)).type;
                        operands.add(new ParserNode(input.substring(i +
                                operator.length()).trim(), this, sourceTable, targetTable));
                    }

//                    nodeText = "substring: " + input;
//                    int parentId = -1;
//                    if (parent != null)
//                        parentId = parent.nodeId;
//                    System.out.printf("%2d  %2d  %9s  :  %1s%n", nodeId, parentId, operator, nodeText);

                    break;
                }
            }

            // if no operators found in the input string, then this is a field value
            if (operator == null) {
                if (input.startsWith("(") && input.endsWith(")")) {
                    input = input.substring(1, input.length() - 1);
                    String[] parts = input.split(",");
                    if (parent.type == DBDefines.STRING) {
                        HashSet<String>
                                left = new HashSet<String>(parts.length);
                        operands.add(left);
                        for (String part : parts) {
                            part = part.trim();
                            if (part.startsWith("'") && part.endsWith("'"))
                                left.add(part.substring(1, part.length() - 1));
                            else
                                throw new DBDefines.FatalDBUtilLibException(
                                        "ERROR in Parser while trying to parse IN clause.  Illegal String value: "
                                                + part);
                        }
                    } else if (parent.type == DBDefines.DOUBLE) {
                        HashSet<Double>
                                left = new HashSet<Double>(parts.length);
                        operands.add(left);
                        for (String part : parts) {
                            part = part.trim();
                            try {
                                left.add(Double.valueOf(part));
                            } catch (NumberFormatException ex) {
                                throw new DBDefines.FatalDBUtilLibException(
                                        "ERROR in Parser while trying to parse IN clause.  Illegal double value: "
                                                + part);
                            }
                        }
                    } else if (parent.type == DBDefines.LONG) {
                        HashSet<Long> left = new HashSet<Long>(parts.length);
                        operands.add(left);
                        for (String part : parts) {
                            part = part.trim();
                            try {
                                left.add(Long.valueOf(part));
                            } catch (NumberFormatException ex) {
                                throw new DBDefines.FatalDBUtilLibException(
                                        "ERROR in Parser while trying to parse IN clause.  Illegal long value: "
                                                + part);
                            }
                        }
                    } else
                        throw new DBDefines.FatalDBUtilLibException(
                                "ERROR in Parser while trying to parse IN clause.  Could not determine javaType of: "
                                        + parts + "  (Expected " +
                                        DBDefines.javaTypes[parent.type] + ")");
                } else if (input.startsWith("'") && input.endsWith("'")) {
                    operands.add(input.substring(1, input.length() - 1));
                    type = DBDefines.STRING;
                } else if (input.startsWith("#") && input.endsWith("#")) {
                    if (sourceTable == null)
                        throw new DBDefines.FatalDBUtilLibException(
                                "ERROR in Parser:  Field value " + input
                                        +
                                        " is not a column in sourceTable because sourceTable is null");

                    sourceIndex = sourceTable.getColumnIndex(
                            input.substring(1, input.length() - 1));
                    if (sourceIndex < 0)
                        throw new DBDefines.FatalDBUtilLibException(
                                "ERROR in Parser:  Field value " + input
                                        + " is not a column in sourceTable " +
                                        sourceTable.getName());

                    type = sourceTable.getColumns()[sourceIndex].getJavaType();
                } else {
                    targetIndex = targetTable.getColumnIndex(input);
                    if (targetIndex >= 0)
                        type = targetTable.getColumns()[targetIndex].getJavaType();
                    else if (parent.type == DBDefines.DOUBLE) {
                        try {
                            operands.add(Double.valueOf(input));
                            type = DBDefines.DOUBLE;
                        } catch (NumberFormatException exDouble) {
                            throw new DBDefines.FatalDBUtilLibException(
                                    "ERROR in Parser: Field value " +
                                            input + " is not of type Double.");
                        }
                    } else if (parent.type == DBDefines.LONG) {
                        try {
                            operands.add(Long.valueOf(input));
                            type = DBDefines.LONG;
                        } catch (NumberFormatException exDouble) {
                            throw new DBDefines.FatalDBUtilLibException(
                                    "ERROR in Parser: Field value " +
                                            input + " is not of type LONG.");
                        }
                    } else if (parent.type == DBDefines.STRING) {
                        operands.add(input);
                        type = DBDefines.STRING;
                    } else {
                        // try to convert to Long.  If not Long, try Double.
                        // If neither then thow error.
                        try {
                            operands.add(Long.valueOf(input));
                            type = DBDefines.LONG;
                        } catch (NumberFormatException exLong) {
                            try {
                                operands.add(Double.valueOf(input));
                                type = DBDefines.DOUBLE;
                            } catch (NumberFormatException exDouble) {
                                throw new DBDefines.FatalDBUtilLibException(
                                        "ERROR in Parser:  Cannot evaluate field value " +
                                                input);
                            }
                        }
                    }
                }

//                if (sourceIndex >= 0)
//                    nodeText = "sourceIndex: " + sourceIndex;
//                else if (targetIndex >= 0)
//                    nodeText = "targetIndex: " + targetIndex;
//                else if (operands.size() > 0)
//                    nodeText = "pointer:     " + operands.get(0).toString();
//                else
//                    nodeText = "?" + input;
//
//                System.out.printf("%2d  %2d  %1s%n", nodeId, parent.nodeId, nodeText);
            }
        }

        public Object evaluate(Row sourceRow, Row targetRow)
                throws FatalDBUtilLibException {
            if (sourceIndex >= 0) {
                if (sourceRow == null)
                    throw new DBDefines.FatalDBUtilLibException(
                            "ERROR in Parser.evaluate(): sourceIndex=" +
                                    sourceIndex + " but sourceRow is null.");

                return sourceRow.getValue(sourceIndex);
            } else if (targetIndex >= 0)
                return targetRow.getValue(targetIndex);
            else if (operator == null) {
                if (operands.size() == 1)
                    return operands.get(0);
                else
                    throw new DBDefines.FatalDBUtilLibException(
                            "ERROR in Parser.evaluate(): operator = null && operands.size() = "
                                    + operands.size());
            } else if (operator.equals("=")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left).equals((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left).equals((Double) right);
                if (type == DBDefines.STRING)
                    return ((String) left).equals((String) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else if (operator.equals(" and "))
                return (Boolean) ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow)
                        &&
                        (Boolean) ((ParserNode) operands.get(1)).evaluate(sourceRow,
                                targetRow);
            else if (operator.equals(" or "))
                return (Boolean) ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow)
                        ||
                        (Boolean) ((ParserNode) operands.get(1)).evaluate(sourceRow,
                                targetRow);
            else if (operator.equals(" in ")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                try {
                    if (type == DBDefines.LONG)
                        return ((HashSet<Long>) right).contains((Long) left);
                    if (type == DBDefines.DOUBLE)
                        return ((HashSet<Double>) right).contains((Double) left);
                    if (type == DBDefines.STRING)
                        return ((HashSet<String>) right).contains((String) left);
                } catch (java.lang.ClassCastException ex) {
                    StringBuilder msg = new StringBuilder();
                    msg.append(
                            "ERROR.  ClassCast Exception in Parser.evaluate() IN clause.\n");
                    msg.append("Right side is of type " +
                            operands.get(0).getClass().getName() + '\n');
                    msg.append("Left  side is of type " +
                            operands.get(1).getClass().getName() + '\n');
                    msg.append("Expected type is " + DBDefines.javaTypes[type] +
                            "\n\n");
                    throw new DBDefines.FatalDBUtilLibException(msg.toString());
                }

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate 'in' clause.\n"
                                + operands.get(0).toString() + " in " +
                                operands.get(1).toString());
            } else if (operator.equals(" between ")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object middle = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(2)).evaluate(sourceRow,
                        targetRow);

                if (type == DBDefines.LONG)
                    return ((Long) left) >= ((Long) middle) &&
                            ((Long) left) <= ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) >= ((Double) middle) &&
                            ((Double) left) <= ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate 'between' clause.\n"
                                + left.toString() + " between " + middle.toString() +
                                " and " + right.toString());
            } else if (operator.equals("+")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.STRING)
                    return ((String) left) + ((String) right);
                if (type == DBDefines.LONG)
                    return ((Long) left) + ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) + ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                " + " + right);
            } else if (operator.equals("-")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) - ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) - ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                " - " + right);
            } else if (operator.equals("<=")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) <= ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) <= ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else if (operator.equals(">=")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) >= ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) >= ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else if (operator.equals("!=")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) != ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) != ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else if (operator.equals("<")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) < ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) < ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else if (operator.equals(">")) {
                Object left = ((ParserNode) operands.get(0)).evaluate(sourceRow,
                        targetRow);
                Object right = ((ParserNode) operands.get(1)).evaluate(sourceRow,
                        targetRow);
                if (type == DBDefines.LONG)
                    return ((Long) left) > ((Long) right);
                if (type == DBDefines.DOUBLE)
                    return ((Double) left) > ((Double) right);

                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate " + left +
                                operator + right);
            } else
                throw new DBDefines.FatalDBUtilLibException(
                        "ERROR in Parser:  Cannot evaluate");

        }

        private void setIndex(HashMap<String, ArrayList<Integer>> index,
                              String op, int depth, int i) {
            ArrayList<Integer> idx = index.get(op);
            if (idx == null) {
                idx = new ArrayList<Integer>();
                index.put(op, idx);
            }
            for (int j = idx.size(); j <= depth; j++)
                idx.add(null);
            if (idx.get(depth) == null)
                idx.add(depth, i);
        }
    }
}
