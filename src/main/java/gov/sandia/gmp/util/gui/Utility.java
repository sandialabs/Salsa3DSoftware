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
package gov.sandia.gmp.util.gui;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.globals.FileDirHandler;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockFileServer;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Static support functions for LSINV. Functions include properties set, date
 * prepend, io directory initialization, row/column block exist test, screen
 * writer output mode set, and properties retrieval and output for tomography,
 * sparse to partitioned, and partitioned to blocked solutions.
 * 
 * @author jrhipp
 *
 */
public class Utility
{
  /**
   * The LSINV directory name.
   */
  public static final String                 aLSINVPathDir         = "LSINV";

  /**
   * Distributed file server directory extension
   */
  public static final String                 aDistrbDir            = "DISTRB";

  /**
   * Creates a PropertiesPlus reader from the input properties file name
   * string.
   * 
   * @param propFileName The file name containing the input properties.
   * @param verbose Optional parameter that if set to true outputs the
   *                property file that is being read.
   * @throws IOException
   */
  public static PropertiesPlus setProperties(String propFileName, boolean ... verbose)
                throws IOException
  {
    if ((verbose.length == 0) || (verbose[0] == true))
      System.out.println(NL + "Reading properties from file: " + propFileName);

    PropertiesPlus props = new PropertiesPlus();
    props.load(new FileInputStream(propFileName));
    props.setProperty("propertiesFileName", propFileName);
    return props;
  }

  /**
   * Prepends the current date string "yyyy_MM_dd" to the front of the
   * input string replacing the occurrence "(DATE)" if it is present.
   * Otherwise the input string is simply returned.
   * 
   * @param inFName The input string for which "(DATE)" will be replaced
   *                with the current date.
   * @return The new input string with "(DATE)" replaced with the current
   *         date string.
   */
  public static String prependDate(String inFName)
  {
    // see if "(DATE)" is present

    int k = inFName.toUpperCase().lastIndexOf("(DATE)");
    if (k > -1)
    {
      // get current date string and replace with current date

      String DATE_FORMAT = "yyyy_MM_dd";
      inFName = inFName.substring(0, k) +
                Globals.getTimeStamp(DATE_FORMAT) +
                inFName.substring(k + 6);
    }

    // return modified (possibly) input string

    return inFName;
  }

  /**
   * Defines, initializes, and sets the "ioDirectory" property.
   * 
   * @param props    The properties file.
   * @param scrnWrtr The screen writer
   * @param appnd    Opens to log file for append if true.
   * @return         The new "ioDirectory" path.
   * @throws IOException
   */
  public static String initializeDirectory(PropertiesPlus props,
                                           ScreenWriterOutput scrnWrtr,
                                           boolean... appnd)
                throws IOException
  {
    return initializeDirectory("", props, scrnWrtr, appnd);
  }

  /**
   * Defines, initializes, and sets the "ioDirectory" property.
   * 
   * @param basePath The input IO directory base path.
   * @param props    The properties file.
   * @param scrnWrtr The screen writer
   * @param appnd    Opens to log file for append if true.
   * @return         The new "ioDirectory" path.
   * @throws IOException
   */
  public static String initializeDirectory(String basePath,
                                           PropertiesPlus props,
                                           ScreenWriterOutput scrnWrtr,
                                           boolean... appnd)
                throws IOException
  {
    String s;

    // define the primary output directory ioDirectory as read from the
    // properties file
    String ioDirectory = props.getPropertyPath("ioDirectory", "").trim();
    ioDirectory = prependDate(ioDirectory);

    // see if it is new or if a number extension is required to avoid
    // overwriting an existing directory of that name

    String newDir = FileDirHandler.newDirectory(basePath, ioDirectory, 1000);
    if (newDir.equals(""))
    {
      s = NL + "  Could not create a new directory: " + ioDirectory + "_#" +
          NL + "# exceeded 1000 limit ..." + NL;
      throw new IOException(s);
    }
    if (!newDir.equals(ioDirectory))
    {
      // found new iodirectory ... set it

      System.out.println("Changing ioDirectory because the ioDirectory " +
                         "already exists: " + ioDirectory);
      System.out.println("Setting new ioDirectory to: " + newDir);
    }

    // create output dir(s) and place a copy of the properties file in the
    // output dir

    if (!basePath.equals("") && (newDir.indexOf(basePath,  0) == -1))
      newDir = basePath + File.separator + newDir;
    props.setProperty("ioDirectory", newDir);
    ioDirectory = newDir;
    FileDirHandler.createOutDir(ioDirectory, props.getProperty("propertiesFileName"));

    // create output file for screen writer if scrnWrtr is defined

    if (scrnWrtr != null)
    {
      FileDirHandler.createOutputLogFileWriter(ioDirectory, scrnWrtr, appnd);
      setOutputWriterMode(props, scrnWrtr);
    }

    return ioDirectory;
  }

  /**
   * Returns true if the block at row, col exists.
   * Otherwise false is returned.
   *  
   * @param typ The type of blocks (matrix) to be checked for existence.
   * @param row The starting row index.
   * @param col The starting column index.
   * 
   * @return true if the block at row, col exists.
   * @throws IOException
   */
  public static boolean rowColBlockExist(MatrixBlockFileServer srvrs,
                                         String typ, int row, int col)
         throws IOException
  {
    String fp = srvrs.getPath(typ, row, col);
    String f = fp + File.separator + typ + "_"  + row + "_" + col;

    File ff = new File(f);
    if (!ff.exists()) return false;
    
    return true;
  }

  /**
   * Sets the screen/file output modes for the GeoTomography, IODB, and LSQR
   * writers based on the output model property
   * 
   * @param props The properties object from which the "outputMode" is read.
   * @param scrnWrtr The ScreenWriterOutput object whose output mode is set.
   * 
   * @throws IOException
   */
  public static void setOutputWriterMode(PropertiesPlus props,
                                         ScreenWriterOutput scrnWrtr)
                throws IOException
  {
    String outputMode = props.getProperty("outputMode", "").trim()
                             .toLowerCase();
    if (outputMode.equalsIgnoreCase("none"))        // turn off all output
      scrnWrtr.setOutputOff();
    else if (outputMode.equalsIgnoreCase("screen")) // turn on screen output only
    {
      scrnWrtr.setScreenOutputOn();
      scrnWrtr.setWriterOutputOff();
    }
    else if (outputMode.equalsIgnoreCase("file"))   // turn on file output only
    {
      scrnWrtr.setWriterOutputOn();
      scrnWrtr.setScreenOutputOff();
    }
    else // default to both on                      // turn on file and screen output
    {
      scrnWrtr.setScreenAndWriterOutputOn();
    }
  }

  /**
   * Returns the partitioned sparse matrix properties object given a path to the
   * directory containing the partitioned sparse matrix. This function is usually
   * called by PartitionedSparseToBlocked.
   * 
   * @param prtSprsMtrxPath The path to the partitioned sparse matrix properties directory
   * 
   * @return The partitioned sparse matrix properties object.
   * @throws IOException
   */
  public static PropertiesPlus getPartitionedSparseMatrixProperties(String prtSprsMtrxPath)
         throws IOException
  {
    String propPath = FileDirHandler.findFile(prtSprsMtrxPath, "properties");
    return setProperties(propPath, false);
  }

  /**
   * Returns the sparse matrix to partitioned sparse matrix properties object
   * given a path to the directory containing the properties file.
   * 
   * @param indent          Amount of space to prepend to each output line.
   * @param prtSprsMtrxPath The path to the properties file.
   * @param scrnWrtr        The input screen writer.
   * @throws IOException
   */
  public static void outputPartitionedSparseMatrixInfo(String indent,
                                                       String prtSprsMtrxPath,
                                                       ScreenWriterOutput scrnWrtr)
         throws IOException
  {
    // decipher path

    PropertiesPlus psmProps = getPartitionedSparseMatrixProperties(prtSprsMtrxPath);
    String sparseMatrixPath = psmProps.getPropertyPath("sparseMatrixPath", "");
    String sparseMatrixFile = psmProps.getProperty("sparseMatrixFile", "");
    String partitionSize    = psmProps.getProperty("partitionSize", "");

    // output partitioned sparse matrix information

    scrnWrtr.write(NL + NL);
    scrnWrtr.write(indent + "Partitioned Sparse Matrix Information:" + NL);
    scrnWrtr.write(indent + "    Output Path           = \"" + prtSprsMtrxPath + "\"" + NL);
    scrnWrtr.write(indent + "    Partitioned File Name = \"" + sparseMatrixFile + "\"" + NL);
    scrnWrtr.write(indent + "    Partitions Size       = " + partitionSize + NL);

    // output tomography sparse matrix information

    outputTomographyInformation(indent, sparseMatrixPath, scrnWrtr);
  }

  /**
   * Returns the tomography properties object given a path to the directory
   * containing the properties file.
   * 
   * @param indent          Amount of space to prepend to each output line.
   * @param sprsMtrxPath    The path to the properties file.
   * @param scrnWrtr        The input screen writer.
   */
  public static void outputTomographyInformation(String indent,
                                                 String sprsMtrxPath,
                                                 ScreenWriterOutput scrnWrtr)
         throws IOException
  {
    // decipher path

    int i = sprsMtrxPath.lastIndexOf(File.separator);
    String tomoPath = sprsMtrxPath.substring(0, i);
    String sprsMtrxDir = sprsMtrxPath.substring(i+1);
    int j = sprsMtrxDir.lastIndexOf("_");
    int iterationStep = Integer.valueOf(sprsMtrxDir.substring(j+1));

    // output header and initial path information

    scrnWrtr.write(NL + NL);
    scrnWrtr.write(indent + "Tomography Information:" + NL);
    scrnWrtr.write(indent + "    Output Path    = \"" + tomoPath + "\"" + NL);
    scrnWrtr.write(indent + "    Iteration Step = " + iterationStep + NL); 
    scrnWrtr.write(indent + "    Starting GeoModel File: \"start_tomo_0.geotess\"" + NL); 
    scrnWrtr.write(indent + "    Starting GeoModel Description:" + NL); 

    // get starting model description and output

    String gmPath = tomoPath + File.separator + "geoModelInfo" +
                    File.separator + "geoModelInfo_1.txt";
    if ((new File(tomoPath)).exists())
    {
      if ((new File(gmPath)).exists())
      {
        FileInputBuffer fib = new FileInputBuffer(gmPath);
        String descr = fib.readString();
        fib.close();
        scrnWrtr.write(Globals.prependLineHeader(descr, indent + "      | ") + NL + NL);
      }
      else
        scrnWrtr.write(indent + "      | *** File Not Found: \"" + gmPath +
                       "\"" + NL + NL);
    }
    else
      scrnWrtr.write(indent + "      | *** Tomography Path Does Not Exist .... \"" +
                     tomoPath + "\"" + NL + NL);

    // output ending model header

    scrnWrtr.write(indent + "    Ending GeoModel File: \"start_tomo_" + iterationStep + ".geotess\"" + NL); 
    scrnWrtr.write(indent + "    Ending GeoModel Description:" + NL); 

    // get ending model description and output

    gmPath = tomoPath + File.separator + "geoModelInfo" +
             File.separator + "geoModelInfo_" + iterationStep + ".txt";
    if ((new File(gmPath).exists()))
    {
      FileInputBuffer fib = new FileInputBuffer(gmPath);
      String descr = fib.readString();
      fib.close();
      scrnWrtr.write(Globals.prependLineHeader(descr, indent + "      | ") + NL + NL);
    }
    else
      scrnWrtr.write(indent + "      | *** File Not Found: \"" + gmPath +
                     "\"" + NL + NL);

    // output sparse matrix information header and path

    scrnWrtr.write(indent + "    Sparse Matrix Description:" + NL); 
    scrnWrtr.write(indent + "        Path         = \"" + tomoPath +
                   File.separator + "restart_" + iterationStep + "\\iterationSparseMatrix\"" + NL); 
    scrnWrtr.write(indent + "        File         = \"sparseMatrix\"" + NL); 

    // get sparse matrix info

    String smPath = sprsMtrxPath + File.separator + "iterationSparseMatrix" + File.separator + "sparseMatrixSize";
    if ((new File(smPath).exists()))
    {
      FileInputBuffer fib = new FileInputBuffer(smPath);
      long[] sze = { 0, 0, 0, 0, 0 };
      sze[0] = fib.readLong();
      sze[1] = fib.readLong();
      sze[2] = fib.readLong();
      sze[3] = fib.readLong();
      sze[4] = fib.readLong();
      fib.close();
      // output sparse matrix info
  
      scrnWrtr.write(indent + "        Entries      = " + sze[2] + NL);
      scrnWrtr.write(indent + "        Rows         = " + sze[0] + NL);
      scrnWrtr.write(indent + "        Columns      = " + sze[1] + NL);
      scrnWrtr.write(indent + "        Observations = " + sze[3] + NL);
      scrnWrtr.write(indent + "        Grid Nodes   = " + sze[4] + NL + NL);
    }
    else
      scrnWrtr.write(indent + "      | *** File Not Found: \"" + smPath +
          "\"" + NL + NL);
  }
}
