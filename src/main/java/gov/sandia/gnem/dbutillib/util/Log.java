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
package gov.sandia.gnem.dbutillib.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class serves as a class to manage message logs. When messages from a
 * Log object are requested, they are returned in the order they were received.
 * <p>The messages are held in memory until {@link #flush flush()} is called when they are
 * written to a file if file printing is turned on.  (See {@link #filePrintOn filePrintOn}.)
 * Messages are written to the screen everytime they are {@link #add added} if console printing
 * is turned on.  (See {@link #consolePrintOn consolePrintOn}).
 * <p>If the number of characters in this log >= {@link #MAX_CHARACTERS MAX_CHARACTERS},
 * a new file will be started.  This file's name will be the original file name followed
 * by . and a number assigned to each new file that is created in numerically increasing
 * order starting from 1.
 */
public class Log {
    /**
     * ArrayList to hold the text messages that are received.
     */
    private ArrayList<String> messages;

    /**
     * Name of this Log Object.
     */
    private String name;

    /**
     * Used for writing out messages to log output files.
     */
    private BufferedWriter bufferedWriter;

    /**
     * Whether or not to print the log messages to the screen.
     */
    private boolean consolePrint;

    /**
     * Whether or not to print the log messages to a file.
     */
    private boolean filePrint;

    /**
     * Total number of characters currently in this Log.  This is maintained so that when the
     * characterTotal >= {@link #MAX_CHARACTERS MAX_CHARACTERS}, a new file can be started.
     * This way files do not become so large that they are unreadable.
     */
    private int characterTotal;

    /**
     * Maximum number of characters to output to a log file before creating a new one.
     */
    private final static int MAX_CHARACTERS = 60000000;

    /**
     * How many new files we have had to use to output this Log's messages.
     */
    private int fileNumber;

    /**
     * If this log is one that is written out to a file, what the name of that
     * file should be.  If the file gets too large, the Log will start being
     * written out to new files named fileName + fileNumber++.
     */
    private String fileName;

    /**
     * Log constructor that takes the name of the Log Object to be created. This
     * allows users to have multiple Log Objects (error, warning, status, etc)
     * that each have their own set of messages.
     *
     * @param name this Log Object's name
     */
    public Log(String name) {
        // Initialize member variables
        this.consolePrint = false;
        this.filePrint = false;
        this.messages = new ArrayList<String>();
        this.name = name;
        this.characterTotal = 0;
        this.fileNumber = 1;
    }

    /**
     * Turn console output on.
     */
    public void consolePrintOn() {
        this.consolePrint = true;
    }

    /**
     * Turn console output off.
     */
    public void consolePrintOff() {
        this.consolePrint = false;
    }

    /**
     * Turn file output on. File output will be written to the file specified
     * in fileName. If the file already exists, it is overwritten.
     *
     * @param fileName name of the file where file output will be written
     */
    public void filePrintOn(String fileName) {
        this.filePrint = true;

        if (fileName == null || fileName.length() == 0) {
            System.err.println("Error in Log.filePrintOn(" + fileName + "). "
                    + fileName + " is null or length 0.");
            return;
        }

        try {
            this.fileName = fileName;

            // Set up a file named fileName that we can write to. If the file
            // already exists, it is overwritten.
            this.bufferedWriter = new BufferedWriter(new FileWriter(this.fileName));
            this.bufferedWriter.write(this.name);
            this.bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("Log.java IOException: " + e.getMessage());
        }
    }

    /**
     * Turn file output off.
     */
    public void filePrintOff() {
        this.filePrint = false;
    }

    /**
     * Add a message to this Log's list of messages.
     *
     * @param message message to be added
     */
    public void add(String message) {
        this.characterTotal += message.length();
        this.messages.add(message);
        if (this.consolePrint)
            System.out.println(message);
    }

    /**
     * Flush the output.
     */
    public void flush() {
        // If filePrint is turned off, then there is nothing to write out.
        if (!this.filePrint) {
            this.messages.clear();
            return;
        }

        if (this.bufferedWriter == null) {
            System.err.println("Error flushing output logs; the file writer for file: " + this.fileName + " is null.");
            return;
        }

        // If the number of characters in the file is >= to MAX_CHARACTERS, flush and close
        // the file and start up a new one.
        if (this.characterTotal >= MAX_CHARACTERS) {
            try {
                // Set up a new file named fileName + . + fileNumber++ that we can
                // write to since the current file is too large. If the file
                // already exists, it is overwritten.
                this.bufferedWriter.flush();
                this.bufferedWriter.close();
                this.fileNumber++;

                this.bufferedWriter = new BufferedWriter(new FileWriter(fileName + "." + fileNumber));

                // Reset the characterTotal
                this.characterTotal = 0;
            } catch (IOException e) {
                System.err.println("Log.java IOException: " + e.getMessage());
            }
        }

        // Write to the file
        try {
            for (String msg : messages) {
                this.bufferedWriter.write(msg);
                this.bufferedWriter.newLine();
            }
            this.bufferedWriter.flush();
        } catch (IOException e) {
            System.err.println("Log.java IOException: " + e.getMessage());
        }

        // Clear out the messages onces they have been written so they don't
        // sit around using up memory.
        clear();
    }

    /**
     * Returns how many messages are in the Log object.
     *
     * @return number of messages in Log object
     */
    public int size() {
        return this.messages.size();
    }

    /**
     * Returns a String version of this Log object.
     *
     * @return String version of this Log object
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (String msg : this.messages)
            s.append(msg + DBDefines.EOLN);
        return s.toString();
    }

    /**
     * Removes all messages from this Log Object.
     */
    public void clear() {
        this.messages.clear();
    }
}
