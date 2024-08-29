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
package gov.sandia.gmp.pcalc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.rayuncertainty.RayUncertainty;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class RayUncertaintyPCalc {
    private ArrayListInt idMap;
    private Set<File> rayWeightBlocksRead = new TreeSet<>(new FileComparator());
    
    /**
     * return true if calculations completed successfully, false if any exceptions 
     * got thrown.  Exception stack traces are appended to the dataSource log file.
     * @param dataSource
     * @param broker
     * @return
     * @throws Exception
     */
    public boolean run(DataLibCorr3D dataSource, ParallelBroker broker)  {
        PropertiesPlus properties = null;

        try {
        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writeln("Computing RayUncertainty.");

        long timer = System.currentTimeMillis();

        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writef("    %s  Build ray uncertainty properties file...%n", dateString());

        // extract ray uncertainty properties from the pcalc properties.
        properties = buildPropertyFile(dataSource);

        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writef("    %s  Run ray uncertainty...%n", dateString());

        runRayUncertainty(properties, broker);

        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writef("    %s  Extract ray uncertainty results...%n", dateString());

        extractUncertaintyValues(properties, dataSource);

        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writef("    %s  Delete temporary files and directories...%n", dateString());

        deleteTemporaryFiles(properties, dataSource.getLog());

        if (dataSource.getLog().getVerbosity() >= 1)
            dataSource.getLog().writef("Computing RayUncertainties completed in %s%n%n", Globals.elapsedTime(timer));

        return true;
        } catch (Exception e) {

        e.printStackTrace();

        try {
            // copy the out.txt file, if it still exists, to log file.
            if (properties != null && new File(properties.getFile("ioDirectory"), "out.txt").exists())
            {
            Scanner input = new Scanner(new File(properties.getFile("ioDirectory"), "out.txt"));
            while (input.hasNextLine())
                dataSource.getLog().writeln(input.nextLine());
            input.close();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        dataSource.getLog().writeln(e);
        }
        return false;
    }

    public void run(PCalc pcalc, Bucket dataBucket, ParallelBroker broker) throws Exception
    {
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writeln("Computing RayUncertainty.");
        
        long timer = System.currentTimeMillis();
        
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writef("    %s  Build ray uncertainty properties file...%n", dateString());
        
        // extract ray uncertainty properties from the pcalc properties.
        PropertiesPlus properties = buildPropertyFile(pcalc, dataBucket);
        
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writef("    %s  Run ray uncertainty...%n", dateString());
        
        runRayUncertainty(properties, broker);
        
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writef("    %s  Extract ray uncertainty results...%n", dateString());
        
        extractUncertaintyValues(properties, pcalc, dataBucket);
        
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writef("    %s  Delete temporary files and directories...%n", dateString());
        
        deleteTemporaryFiles(properties, pcalc.log);
        
        if (pcalc.log.getVerbosity() >= 1)
            pcalc.log.writef("Computing RayUncertainties completed in %s%n%n", Globals.elapsedTime(timer));
    }
    
    public Set<File> getRayWeightBlocksRead(){ return rayWeightBlocksRead; }
    
    private void deleteTemporaryFiles(PropertiesPlus properties, ScreenWriterOutput log) 
    {
        try
        {
            // write a file named DONE in the ioDirectory indicating that
            // RayUncertainty has completed and that the directory can be deleted.
            File f = properties.getFile("ioDirectory");
            if (f!= null)
            {
                f = new File(f, "DONE");
                FileWriter fw = new FileWriter(f);
                fw.write("RayUncertaintyPCalc completed all work and the directory \n"
                        + "containing this file should have been deleted \n"+dateString());
                fw.close();
            }
        }
        catch (Exception ex) {
            log.writeln(ex);    
        }
        
        // Rename the properties file from rayuncertainty-sta-yyyy-MM-dd-HH-mm-ss-SSS.properties
        // to deleteme-sta-yyyy-MM-dd-HH-mm-ss-SSS.properties
        File propertiesFile = null;
        try {
            propertiesFile = properties.getFile("propertiesFileName");
            if (propertiesFile != null)
            {
                File newFileName = new File(propertiesFile.getParentFile(), 
                        propertiesFile.getName().replace("rayuncertainty", "deleteme"));
                if (propertiesFile.renameTo(newFileName))
                    propertiesFile = newFileName;
            }
        }
        catch (Exception ex) {
            log.writeln(ex);    
        }
        
        // delete the ray uncertainty properties file (created by buildPropertyFile method).
        try {
            propertiesFile.delete();
        } catch (Exception ex) {
            log.writeln(ex);
        }
        
        // Rename the iodirectory from rayuncertainty-yyyy-MM-dd-HH-mm-ss-SSS
        // to deleteme-yyyy-MM-dd-HH-mm-ss-SSS
        File ioDirectory = null;
        try {
            ioDirectory = properties.getFile("ioDirectory");
            if (ioDirectory != null)
            {
                File newFileName = new File(ioDirectory.getParentFile(), 
                        ioDirectory.getName().replace("rayuncertainty", "deleteme"));
                if (ioDirectory.renameTo(newFileName))
                    ioDirectory = newFileName;
            }
        }
        catch (Exception ex) {
            log.writeln(ex);    
        }
        
        try {
            // delete the temporary ray uncertainty work directory. Try 10 times.
            for (int i=0; i<10; ++i)
            {
                Thread.sleep(1000);
                deleteFile(ioDirectory);
                Thread.sleep(1000);
                if (!ioDirectory.exists())
                    break;
            }
        } catch (Exception e) {
            log.writeln(e);
        }
    }
        
    private void extractUncertaintyValues(PropertiesPlus properties, DataLibCorr3D dataSource) throws Exception
    {
        File ioDirectory = properties.getFile("ioDirectory");       
        
        Scanner input = new Scanner(new File(ioDirectory, "out.txt"));
        while (input.hasNextLine())
            dataSource.getLog().writeln(input.nextLine());
        input.close();
        
        PointMap pointMap = dataSource.getModel().getPointMap();
        for (int pt=0; pt < pointMap.size(); ++pt)
            pointMap.setPointValue(pt, 1, Double.NaN);
        
        // copy uncertainty results from the uncertainty.txt file to the dataBucket.modelVallues array
        input = new Scanner(new File(ioDirectory, "variance.txt"));
        String line = input.nextLine();
        while (!line.trim().startsWith("(sec^2)"))
            line = input.nextLine();
        int count = 0;
        while (input.hasNext())
        {
            line = input.nextLine();
            ++count;
            Scanner in = new Scanner(line);
            in.next(); // ignore sta 
            
            pointMap.setPointValue(idMap.get(in.nextInt()), 1, Math.sqrt(in.nextDouble()));
            
            in.close();
        }
        input.close();
        dataSource.getLog().writef("%d uncertainty values computed. %d valid, %d invalid (set to NaN)%n",
                pointMap.size(), count, (pointMap.size()-count));
    }
    
    private void extractUncertaintyValues(PropertiesPlus properties, PCalc pcalc, Bucket dataBucket) throws Exception
    {
        File ioDirectory = properties.getFile("ioDirectory");       
        
        Scanner input = new Scanner(new File(ioDirectory, "out.txt"));
        while (input.hasNextLine())
            pcalc.log.writeln(input.nextLine());
        input.close();
        
        // copy uncertainty results from the uncertainty.txt file to the dataBucket.modelVallues array
        int idx = pcalc.outputAttributes.indexOf(GeoAttributes.TT_MODEL_UNCERTAINTY)+1;
        input = new Scanner(new File(ioDirectory, "variance.txt"));
        String line = input.nextLine();
        while (!line.trim().startsWith("(sec^2)"))
            line = input.nextLine();
        int count = 0;
        while (input.hasNext())
        {
            line = input.nextLine();
            ++count;
            Scanner in = new Scanner(line);
            in.next(); // ignore sta 
            dataBucket.modelValues[idMap.get(in.nextInt())][idx] = Math.sqrt(in.nextDouble());
            in.close();
        }
        input.close();
        pcalc.log.writef("%d uncertainty values computed. %d valid, %d invalid (set to NaN)%n",
                dataBucket.modelValues.length, count, 
                (dataBucket.modelValues.length-count));
    }
    
    private void runRayUncertainty(PropertiesPlus properties, ParallelBroker broker)
        throws Exception
    {
        RayUncertainty ru = new RayUncertainty();
        
        //TODO print these things out to figure out why it isn't maxing out the cluster:
        // Max requested number of tasks-in-flight:
        int procs = Math.max(Runtime.getRuntime().availableProcessors(),
            broker.getProcessorCountEstimate());
        
        int maxTskSubmsnLimit = procs*5;
        
        // Tasks per batched submission:
        int batchSize = Math.max(procs/8,8);
        
        // Simultaneous batch submission limit:
        int batchLimit = Math.max(2, maxTskSubmsnLimit/batchSize +
            maxTskSubmsnLimit%batchSize > 0 ? 1 : 0);
        
        broker.setBatchSize(batchSize);
        broker.setMaxBatches(batchLimit);
        
        ru.initializeSolution(properties.getFile("propertiesFileName").getAbsolutePath(),
            broker);
        ru.solve();
        rayWeightBlocksRead.addAll(ru.getRayWeightBlocksRead());
        
        if (ru.getGUI() != null) ru.getGUI().dispose();
        ru = null;
        // call the garbage collector
        System.gc();
    }
    
    private PropertiesPlus buildPropertyFile(PCalc pcalc, Bucket dataBucket) throws Exception
    {
            PropertiesPlus pcalcProperties = pcalc.properties;

        File workDir = pcalcProperties.getFile("benderUncertaintyWorkDir");
        if (workDir == null)
            throw new Exception("Must specify property benderUncertaintyWorkDir in the properties file.");
        workDir.mkdirs();
        
        File benderModel = pcalcProperties.getFile("benderModel");
        if (benderModel == null)
            throw new Exception("Property benderModel not specified in the properties file.");
        
        if (!benderModel.exists())
            throw new Exception(String.format("benderModel %s does not exist.", benderModel.getAbsoluteFile()));
        
        if (!benderModel.isDirectory())
            throw new Exception(String.format("benderModel %s is not a directory.", benderModel.getAbsoluteFile()));
        
        File tomoModel = new File(benderModel, "tomo_model.geotess");

        if (!tomoModel.exists())
            throw new Exception(String.format("Properties file specifies benderModel=%s%nbut that directory does not contain tomo_model.geotess.",
                    benderModel.getAbsolutePath()));
        
        File ioDirectory = getWorkDir(workDir, pcalc.bucket.site.getSta());
        
        if (ioDirectory.exists())
            throw new Exception("Unable to create a new, empty, bender uncertainty work directory "
                    +ioDirectory.getAbsolutePath());
        
        File stdevFile = new File(benderModel, "layer_standard_deviations.properties");
        if (!stdevFile.exists())
            throw new Exception("File "+stdevFile.getAbsolutePath()+" does not exist.");
        PropertiesPlus stddev = new PropertiesPlus(stdevFile);
        
        pcalcProperties.setProperty("rayUncertaintyIODirectory", ioDirectory.getCanonicalPath());
        
        PropertiesPlus p = new PropertiesPlus();
        p.setProperty("ioDirectory", ioDirectory.getCanonicalPath());

        p.setProperty("startPhase", pcalcProperties.getProperty("startPhase", "PREDICTION"));
        p.setProperty("endPhase", pcalcProperties.getProperty("endPhase", "RAY_UNCERTAINTY"));
        p.setProperty("outputMode", "both");
        p.setProperty("phaseDefinition", pcalcProperties.getProperty("phase"));
        p.setProperty("storagePrecision", pcalcProperties.getProperty("geotessDataType", "FLOAT").toUpperCase());

        if (pcalcProperties.containsKey("slownessLayerStandardDeviation_P"))
            p.setProperty("slownessLayerStandardDeviation_P", pcalcProperties.getProperty("slownessLayerStandardDeviation_P"));
        else
        {
            p.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", ""));
            pcalcProperties.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", "unspecified"));
        }           
        
        if (pcalcProperties.containsKey("slownessLayerStandardDeviation_S"))
            p.setProperty("slownessLayerStandardDeviation_S", pcalcProperties.getProperty("slownessLayerStandardDeviation_S"));
        else        
        {
            p.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", ""));
            pcalcProperties.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", "unspecified"));
        }           
        
        p.setProperty("geoModelTomographyPath", tomoModel.getParent());
        p.setProperty("geoModelTomographyFileName", tomoModel.getName());

        p.setProperty("covarianceMatrixActiveNodeMapPath", benderModel.getAbsolutePath());
        p.setProperty("covarianceFileServerPaths", benderModel.getAbsolutePath());

        p.setProperty("covarianceMatrixActiveNodeMapFileName", "activenodeIndexMap");
        p.setProperty("covarianceFileServerBlockMap", pcalcProperties.getProperty("covarianceFileServerBlockMap", ""));
        p.setProperty("covarianceFileServerStorageUseFraction", pcalcProperties.getProperty("covarianceFileServerStorageUseFraction", ""));
        p.setProperty("covarianceFileSecondaryPaths", pcalcProperties.getProperty("covarianceFileSecondaryPaths", "cov ginv"));

        
        p.setProperty("displayGUI", pcalcProperties.getBoolean("displayGUI", false));
        
        if (pcalcProperties.containsKey("parallelMode")) 
            p.setProperty("parallelMode", pcalcProperties.getProperty("parallelMode"));
        if (pcalcProperties.containsKey("maxProcessors")) 
            p.setProperty("maxProcessors", pcalcProperties.getProperty("maxProcessors"));
        
        if (pcalcProperties.containsKey("fabricApplicationName")) 
            p.setProperty("fabricApplicationName", pcalcProperties.getProperty("fabricApplicationName"));
        if (pcalcProperties.containsKey("fabricMaxThreadsPerNode")) 
            p.setProperty("fabricMaxThreadsPerNode", pcalcProperties.getProperty("fabricMaxThreadsPerNode"));
        if (pcalcProperties.containsKey("fabricBaselineNodeMemory")) 
            p.setProperty("fabricBaselineNodeMemory", pcalcProperties.getProperty("fabricBaselineNodeMemory"));
        if (pcalcProperties.containsKey("nodeMaxMemory")) 
            p.setProperty("nodeMaxMemory", pcalcProperties.getProperty("nodeMaxMemory"));
        if (pcalcProperties.containsKey("driverMaxMemory")) 
            p.setProperty("driverMaxMemory", pcalcProperties.getProperty("driverMaxMemory"));
        if (pcalcProperties.containsKey("taskTimeout")) 
            p.setProperty("taskTimeout", pcalcProperties.getProperty("taskTimeout"));

        p.setProperty("receiverDefinition", "");
        Site site = pcalc.bucket.site;
        if (site == null)
            throw new Exception("Site is undefined.");
        p.setProperty("receiverDefinitionList", String.format("%s %d %d %1.6f %1.6f %6.3f %s %s %1.3f %1.3f",
                site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), site.getLon(), site.getElev(),
                site.getStatype(), site.getRefsta(), site.getDnorth(), site.getDeast()));

        p.setProperty("sourceDefinition", "PROPERTIESFILE");
        StringBuffer sourceDefinitionList = new StringBuffer();
//      for (GeoVector point : dataBucket.points)
//          sourceDefinitionList.append(point.toString(";%1.6f,%1.6f,%1.3f"));
        // find the index of tt_delta_ak135 in the output attributes.
        int ttid = Math.max(pcalc.outputAttributes.indexOf(GeoAttributes.TT_PATH_CORRECTION),
                pcalc.outputAttributes.indexOf(GeoAttributes.TT_DELTA_AK135));
        idMap = new ArrayListInt(dataBucket.points.size());
        for (int i=0; i<dataBucket.points.size(); ++i)
        {
            // get the value of tt_delta_ak135
            double tt = dataBucket.modelValues[i][ttid+1];
            
            // if tt_delta_ak135 is valid, add the source to the sourceDefinitionList
            if (!Double.isNaN(tt) && tt != Globals.NA_VALUE)
            {
                sourceDefinitionList.append(dataBucket.points.get(i).toString(";%1.6f,%1.6f,%1.3f"));
                idMap.add(i);
            }
        }
        p.setProperty("sourceDefinitionList", sourceDefinitionList.toString().substring(1));

        File rayPropertiesFileName = new File(ioDirectory.getAbsoluteFile()+".properties");
        p.setProperty("propertiesFileName", rayPropertiesFileName.getAbsolutePath());
        
        String comment = String.format("Temporary properties file created automatically by %s\n"
                + "which will be used by RayUncertainty to compute path dependent travel time uncertainty values",
        getClass().getName());
        
        FileOutputStream fos = new FileOutputStream(rayPropertiesFileName);
        
        p.store(fos, comment);
        
        fos.close();

        return p;
    }
    
    private PropertiesPlus buildPropertyFile(DataLibCorr3D dataSource) throws Exception
    {

        File workDir = dataSource.getProperties().getFile("benderUncertaintyWorkDir");
        if (workDir == null)
            throw new Exception("Must specify property benderUncertaintyWorkDir in the properties file.");
        workDir.mkdirs();
        
        File benderModel = dataSource.getProperties().getFile("benderModel");
        if (benderModel == null)
            throw new Exception("Property benderModel not specified in the properties file.");
        
        if (!benderModel.exists())
            throw new Exception(String.format("benderModel %s does not exist.", benderModel.getAbsoluteFile()));
        
        if (!benderModel.isDirectory())
            throw new Exception(String.format("benderModel %s is not a directory.", benderModel.getAbsoluteFile()));
        
        File tomoModel = new File(benderModel, "tomo_model.geotess");

        if (!tomoModel.exists())
            throw new Exception(String.format("Properties file specifies benderModel=%s%nbut that directory does not contain tomo_model.geotess.",
                    benderModel.getAbsolutePath()));
        
        File ioDirectory = getWorkDir(workDir, dataSource.getSite().getSta());
        
        if (ioDirectory.exists())
            throw new Exception("Unable to create a new, empty, bender uncertainty work directory "
                    +ioDirectory.getAbsolutePath());
        
        File stdevFile = new File(benderModel, "layer_standard_deviations.properties");
        if (!stdevFile.exists())
            throw new Exception("File "+stdevFile.getAbsolutePath()+" does not exist.");
        PropertiesPlus stddev = new PropertiesPlus(stdevFile);
        
        dataSource.getProperties().setProperty("rayUncertaintyIODirectory", ioDirectory.getCanonicalPath());
        
        PropertiesPlus p = new PropertiesPlus();
        p.setProperty("ioDirectory", ioDirectory.getCanonicalPath());

        p.setProperty("startPhase", dataSource.getProperties().getProperty("startPhase", "PREDICTION"));
        p.setProperty("endPhase", dataSource.getProperties().getProperty("endPhase", "RAY_UNCERTAINTY"));
        p.setProperty("outputMode", "both");
        p.setProperty("phaseDefinition", dataSource.getProperties().getProperty("phase"));
        p.setProperty("storagePrecision", dataSource.getProperties().getProperty("geotessDataType", "FLOAT").toUpperCase());

        if (dataSource.getProperties().containsKey("slownessLayerStandardDeviation_P"))
            p.setProperty("slownessLayerStandardDeviation_P", dataSource.getProperties().getProperty("slownessLayerStandardDeviation_P"));
        else
        {
            p.setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", ""));
            dataSource.getProperties().setProperty("slownessLayerStandardDeviation_P", stddev.getProperty("slownessLayerStandardDeviation_P", "unspecified"));
        }           
        
        if (dataSource.getProperties().containsKey("slownessLayerStandardDeviation_S"))
            p.setProperty("slownessLayerStandardDeviation_S", dataSource.getProperties().getProperty("slownessLayerStandardDeviation_S"));
        else        
        {
            p.setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", ""));
            dataSource.getProperties().setProperty("slownessLayerStandardDeviation_S", stddev.getProperty("slownessLayerStandardDeviation_S", "unspecified"));
        }           
        
        p.setProperty("geoModelTomographyPath", tomoModel.getParent());
        p.setProperty("geoModelTomographyFileName", tomoModel.getName());

        p.setProperty("covarianceMatrixActiveNodeMapPath", benderModel.getAbsolutePath());
        p.setProperty("covarianceFileServerPaths", benderModel.getAbsolutePath());

        p.setProperty("covarianceMatrixActiveNodeMapFileName", "activenodeIndexMap");
        p.setProperty("covarianceFileServerBlockMap", dataSource.getProperties().getProperty("covarianceFileServerBlockMap", ""));
        p.setProperty("covarianceFileServerStorageUseFraction", dataSource.getProperties().getProperty("covarianceFileServerStorageUseFraction", ""));
        p.setProperty("covarianceFileSecondaryPaths", dataSource.getProperties().getProperty("covarianceFileSecondaryPaths", "cov ginv"));

        
        p.setProperty("displayGUI", dataSource.getProperties().getBoolean("displayGUI", false));
        
        if (dataSource.getProperties().containsKey("parallelMode")) 
            p.setProperty("parallelMode", dataSource.getProperties().getProperty("parallelMode"));
        if (dataSource.getProperties().containsKey("maxProcessors")) 
            p.setProperty("maxProcessors", dataSource.getProperties().getProperty("maxProcessors"));
        
        if (dataSource.getProperties().containsKey("fabricApplicationName")) 
            p.setProperty("fabricApplicationName", dataSource.getProperties().getProperty("fabricApplicationName"));
        if (dataSource.getProperties().containsKey("fabricMaxThreadsPerNode")) 
            p.setProperty("fabricMaxThreadsPerNode", dataSource.getProperties().getProperty("fabricMaxThreadsPerNode"));
        if (dataSource.getProperties().containsKey("fabricBaselineNodeMemory")) 
            p.setProperty("fabricBaselineNodeMemory", dataSource.getProperties().getProperty("fabricBaselineNodeMemory"));
        if (dataSource.getProperties().containsKey("nodeMaxMemory")) 
            p.setProperty("nodeMaxMemory", dataSource.getProperties().getProperty("nodeMaxMemory"));
        if (dataSource.getProperties().containsKey("driverMaxMemory")) 
            p.setProperty("driverMaxMemory", dataSource.getProperties().getProperty("driverMaxMemory"));
        if (dataSource.getProperties().containsKey("taskTimeout")) 
            p.setProperty("taskTimeout", dataSource.getProperties().getProperty("taskTimeout"));

        p.setProperty("receiverDefinition", "");
        Site site = dataSource.getSite();
        if (site == null)
            throw new Exception("Site is undefined.");
        p.setProperty("receiverDefinitionList", String.format("%s %d %d %1.6f %1.6f %6.3f %s %s %1.3f %1.3f",
                site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), site.getLon(), site.getElev(),
                site.getStatype(), site.getRefsta(), site.getDnorth(), site.getDeast()));

        p.setProperty("sourceDefinition", "PROPERTIESFILE");
        StringBuffer sourceDefinitionList = new StringBuffer();
        PointMap pointMap = dataSource.getModel().getPointMap();
        int ttid = 0;
        idMap = new ArrayListInt(dataSource.getModel().getNPoints());
        for (int i=0; i<pointMap.size(); ++i)
        {
            // get the value of tt_delta_ak135
                
            double tt = pointMap.getPointValueDouble(i, ttid);
            
            // if tt_delta_ak135 is valid, add the source to the sourceDefinitionList
            if (!Double.isNaN(tt) && tt != Globals.NA_VALUE)
            {
                sourceDefinitionList.append(String.format(";%1.6f,%1.6f,%1.4f", 
                    pointMap.getPointLatitudeDegrees(i), 
                    pointMap.getPointLongitudeDegrees(i),
                    pointMap.getPointDepth(i)));
                idMap.add(i);
            }
        }
        p.setProperty("sourceDefinitionList", sourceDefinitionList.toString().substring(1));

        File rayPropertiesFileName = new File(ioDirectory.getAbsoluteFile()+".properties");
        p.setProperty("propertiesFileName", rayPropertiesFileName.getAbsolutePath());
        
        String comment = String.format("Temporary properties file created automatically by %s\n"
                + "which will be used by RayUncertainty to compute path dependent travel time uncertainty values",
        getClass().getName());
        
        FileOutputStream fos = new FileOutputStream(rayPropertiesFileName);
        
        p.store(fos, comment);
        
        fos.close();

        return p;
    }
    
    /**
     * Delete the specified file from the file system.  If the specified
     * file is a directory, this method will recursively delete all the 
     * files and directories in the specified directory before deleting the
     * directory itself.
     * @param file
     */
    private boolean deleteFile(File file)
    {
        if (file == null)
            return false;
        
        if (file.isDirectory())
            for (File f : file.listFiles())
                deleteFile(f);
        boolean deleted = false;
        try {
            deleted = java.nio.file.Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("directory "+file.getAbsolutePath()+" is not empty.\nContents:\n");
            for (String f : file.list())
                System.out.println(f);
        }
        return deleted;
    }
    
    /**
     * Get a File consisting of root/rayuncertainty-yyyy-MM-dd-HH-mm-ss-SSS
     * @param root
     * @return
     * @throws Exception
     */
    private synchronized File getWorkDir(File root, String sta) throws Exception
    {
        Thread.sleep(50);
        String date = null;
        try {
            date = dateString();
        } catch (Exception e) { 
            date = String.format("%d", System.currentTimeMillis());
        }
        return new File(root, String.format("rayuncertainty-%s-%s-%s",
                date, sta, Globals.getComputerName()));
    }
    
    private String dateString() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
    }
    
    private static class FileComparator implements Comparator<File>, Serializable {
      private static final long serialVersionUID = 1L;

      @Override
      public int compare(File f1, File f2) {
        return f1.getPath().compareTo(f2.getPath());
      }
    }
}
