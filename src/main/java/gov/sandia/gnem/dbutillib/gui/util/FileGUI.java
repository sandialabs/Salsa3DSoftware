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
package gov.sandia.gnem.dbutillib.gui.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import gov.sandia.gnem.dbutillib.util.DBDefines;

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
public class FileGUI
        //extends JFileChooser
{

    public FileGUI() {
    }

    /**
     * This is a general method to launch a file browser. It is flexible and
     * allows the caller to specify selection of a file or directory, to pass in a
     * tool tip, a dialog title, the label for the approval button, as well as the
     * current directory.
     *
     * @param isFile        true = Select a File, false = Select a Directory
     * @param tool_tip_text Tool tip text to display
     * @param dialog_title  Title of the file browser
     * @param approve_btn   Label for the approval (select) button
     * @param curr_dir      Directory from which to browse
     * @param substring     String[]
     * @param extension     String[]
     * @return File Selected file or directory
     */
    public static String getFile(boolean isFile, String tool_tip_text,
                                 String dialog_title, String approve_btn,
                                 String curr_dir, String[] substring,
                                 String[] extension) {
        // author is Paul Reeves.
        File file = null;

        JFileChooser fc;
        fc = new JFileChooser();
        if (isFile)
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        else
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Set Custom File Chooser Properties
        fc.setApproveButtonToolTipText(tool_tip_text);
        fc.setDialogTitle(dialog_title);
        fc.setApproveButtonText(approve_btn);

        // Set File Filtering
        FileNameFilter filter = new FileNameFilter(substring, extension);
        fc.setFileFilter(filter);

        if (curr_dir != null) {
            File curr_dir_file = new File(curr_dir);
            fc.setCurrentDirectory(curr_dir_file);
        }
        fc.setDialogType(JFileChooser.OPEN_DIALOG);

//  // Set up a frame to use
        JFrame prompt = new JFrame();
        prompt.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        int returnVal = fc.showOpenDialog(prompt);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // File Chosen
            file = fc.getSelectedFile();
            try {
                return file.getCanonicalPath();
            } catch (Exception e) {
                String error = "Error in FileGui.getFile.\nError message: "
                        + e.getMessage();
                DBDefines.ERROR_LOG.add(error);
            }
        } else if (returnVal == JFileChooser.CANCEL_OPTION)
            return null;

        return "";
    }


    static class FileNameFilter extends FileFilter {

        private String[] substring = null;

        private String[] extension = null;

        /**
         * Creates a file filter. If no filters are added, then all files are
         * accepted.
         */
        public FileNameFilter(String[] ss, String[] suf) {
            substring = ss;
            extension = suf;
        }

        /**
         * Return true if A) The filename contains any of the specified substrings (if
         * any), and B) The file extension matches any of the specified suffixes
         * <p>
         * Directories and files that begin with "." are ignored.
         */
        @Override
        public boolean accept(File f) {

            if (f != null) {
                // Accept directories
                if (f.isDirectory()) {
                    // System.out.println("...is a directory");
                    return true;
                }

                // Check substrings and extensions

                // System.out.println("f.getname()="+f.getName());

                // if (containsSubString(f.getName()))
                // System.out.println("...contains the substring");

                // if (hasExtension(f.getName()))
                // System.out.println("...is of type");

                return (containsSubString(f.getName()) && hasExtension(f.getName()));

            }
            return false;
        }

        /**
         * Check if filename contains a substring from the array of {@link #substring substrings}.
         *
         * @param filename to check to see if it contains a substring from the array of {@link #substring substrings}.
         * @return true if filename contains a substring from the array of {@link #substring substrings}; false
         * otherwise
         */
        private boolean containsSubString(String filename) {
            // If no substring filters have been specified, then
            // accept all filenames
            // System.out.println("filename: "+ filename);

            if (substring == null)
                return true;

            String prefix = getPrefix(filename);

            // System.out.println("prefix: ("+ prefix + ")");

            if (prefix == null)
                return false;

            boolean flag = false;
            for (int i = 0; i < substring.length; i++) {
                if (prefix.contains(substring[i]) || prefix.contains(substring[i].toUpperCase())
                        || prefix.contains(substring[i].toLowerCase()))
                    flag = true;
            }
            return flag;
        }

        private boolean hasExtension(String filename) {
            if (extension == null)
                return true;

            String suffix = getExtension(filename);

            if (suffix == null)
                return false;

            boolean flag = false;
            for (int i = 0; i < extension.length; i++) {
                if (suffix.equals(extension[i]))
                    flag = true;
            }
            return flag;
        }

        /**
         * Returns the prefix portion of the filename
         *
         * @param filename Complete filename, e.g., 'myfile.txt'
         * @return Prefix of filename, e.g., 'myfile'
         */
        public String getPrefix(String filename) {
            if (filename != null) {
                int i = filename.lastIndexOf('.');
                if (i > 1) {
                    return filename.substring(0, i);
                }
            }
            return null;
        }

        /**
         * Returns the suffix (i.e., extension) portion of the filename
         *
         * @param filename Complete filename, e.g., 'myfile.txt'
         * @return Prefix of filename, e.g., 'txt'
         */
        public String getExtension(String filename) {
            if (filename != null) {
                int i = filename.lastIndexOf('.');
                if (i > 0 && i < filename.length() - 1) {
                    return filename.substring(i + 1);
                }
            }
            return null;
        }

        @Override
        public String getDescription() {
            StringBuilder part1 = null;
            if (substring != null) {
                part1 = new StringBuilder("filenames containing ");
                for (int i = 0; i < substring.length - 1; i++) {
                    part1.append(substring[i]);
                    part1.append(", ");
                }
                part1.append(substring[substring.length - 1]);
            }
            /*
             * StringBuilder part2 = null; if (extension != null) { part2 = new
             * StringBuilder("files of type "); for (int i = 0; i < extension.length-1;
             * i++) { part2.append(extension[i]); part2.append(", "); }
             * part2.append(extension[extension.length-1]); }
             */
            StringBuilder part2 = null;
            if (extension != null) {
                part2 = new StringBuilder("");
                for (int i = 0; i < extension.length - 1; i++) {
                    part2.append("*.");
                    part2.append(extension[i]);
                    part2.append(", ");
                }
                part2.append("*.");
                part2.append(extension[extension.length - 1]);
            }
            /*
             * if (part1 == null && part2 == null) return "No files will be filtered";
             */
            if (part1 == null && part2 == null)
                return "*.*";
            else if (part1 != null && part2 == null)
                return part1.toString();
            else if (part1 == null && part2 != null)
                return part2.toString();
            else if (part1 != null && part2 != null)
                return part1.toString() + " and " + part2.toString();
            else
                return "No Description Available";

        }
    } // end of FileFilter
}// end of FileGUI
