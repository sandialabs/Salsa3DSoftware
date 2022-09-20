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

import java.util.ArrayList;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Site;

public class DataSourceFilePredictions extends DataSourceFile
{
  private int staIndex = -1;
  private int jdateIndex = -1;
  private int siteLatIndex=-1;
  private int siteLonIndex=-1;
  private int siteDepthIndex=-1;
  private int siteOndateIndex=-1;
  private double sign=1;
  private int phaseIndex=-1;

  private int sourceLatIndex;
  private int sourceLonIndex;
  private int sourceDepthIndex;

  public DataSourceFilePredictions(PCalc pcalc) throws GMPException
  {
    super(pcalc);

    batchSize = properties.getInt("batchSize", 10000);

    try
    {
      // deal with inputAttributes

      String line;
      if (properties.getBoolean("inputHeaderRow", false))
      {
        line = input.nextLine().trim();
        while (line.startsWith("#") || line.length()==0)
        {
          comments.add(line);
          line = input.nextLine().trim();
        }
        inputHeader = line;
        if (log.isOutputOn())
          log.write(String.format("inputAttributes read from first line of input file.%n%n"));

        if (inputHeader.toLowerCase().indexOf("origin_lat") < 0)
          throw new GMPException(String.format(
              "%ninputHeader = %s%ndoes not contain required elements. Should property inputHeaderRow = false?", 
              inputHeader));
      }
      else
      {
        line = properties.getProperty("inputAttributes", 
            "sta jdate site_lat site_lon site_elev origin_lat origin_lon origin_depth phase");
        if (log.isOutputOn())
          log.write(String.format("inputAttributes read from property file.%n%n"));
      }

      // extract the names of all the columns from the header row.
      Scanner scanner = new Scanner(line.replaceAll(",", " "));
      while (scanner.hasNext())
        bucket.inputAttributes.add(scanner.next().trim().toLowerCase());
      scanner.close();

      // build the input header from the list of column names
      if (inputHeader == null || inputHeader.length() == 0)
        setInputHeader();

      if (log.isOutputOn())
        log.writeln("parsed inputAttributes = "+inputHeader);

      // build a map from the name of an input attribute to the index of the attribute in each input record.
      for (int i=0; i<bucket.inputAttributes.size(); ++i)
        inputMap.put(bucket.inputAttributes.get(i).trim(), i);

      sourceLatIndex = inputMap.get("origin_lat");
      sourceLonIndex = inputMap.get("origin_lon");
      if (inputMap.containsKey("origin_depth"))
        sourceDepthIndex = inputMap.get("origin_depth");
      else
        pcalc.extractDepthInfo(bucket);

      if (inputMap.containsKey("phase"))
        phaseIndex = inputMap.get("phase");
      else if (properties.containsKey("phase"))
      {
        bucket.phases = new ArrayList<SeismicPhase>(1);
        bucket.phases.add(properties.getSeismicPhase("phase"));
      }
      else 
        throw new GMPException("\nProperty phase is not specified in property file.");

      if (inputMap.containsKey("jdate"))
        jdateIndex = inputMap.get("jdate");
      else if (properties.containsKey("jdate"))
      {
        bucket.time = new ArrayListDouble(1);
        bucket.time.add(GMTFormat.getEpochTime(properties.getInt("jdate")));
      }
      else 
        throw new GMPException("\nProperty jdate is not specified in property file.");

      if (inputMap.containsKey("site_lat"))
      {
        // get the indexes of the required elements.
    	if (inputMap.containsKey("site_sta"))
    		staIndex = inputMap.get("site_sta");
    	else if (inputMap.containsKey("sta"))
    		staIndex = inputMap.get("sta");
    	else
    		staIndex = -1;
        siteLatIndex = inputMap.get("site_lat");
        siteLonIndex = inputMap.get("site_lon");
        siteDepthIndex = -1;
        sign = 0;
        if (inputMap.containsKey("site_elev"))
        {
          siteDepthIndex = inputMap.get("site_elev");
          sign = -1.;
        }
        else if (inputMap.containsKey("site_depth"))
        {
          siteDepthIndex = inputMap.get("site_depth");
          sign = 1.;
        }
        siteOndateIndex = inputMap.get("site_ondate") == null ? -1 : inputMap.get("site_ondate");
      }
      else if (properties.containsKey("site"))
      {
        siteLatIndex = -1;
        bucket.site = PCalc.getSite(properties);

        Receiver receiver = new Receiver(bucket.site.getSta(), (int)bucket.site.getOndate(), (int)bucket.site.getOffdate(),
        		bucket.site.getLat(), bucket.site.getLon(), bucket.site.getElev(), bucket.site.getStaname(), bucket.site.getStatype(),
        		bucket.site.getRefsta(), bucket.site.getDnorth(), bucket.site.getDeast());
        bucket.receivers = new ArrayList<Receiver>(1);
        bucket.receivers.add(receiver);

        bucket.time = new ArrayListDouble(1);
        bucket.time.add(GMTFormat.getEpochTime(
            properties.getInt("jdate", (int)Site.OFFDATE_NA)));
      }
      else 
        throw new GMPException("inputAttributes does not contain [site_lat, site_lon, site_elev/site_depth] "
            +"nor do properties contain [site]");
    } 
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  public boolean hasNext() {
    return input.hasNext();
  }

  @Override
  public Bucket next()
  {
    Bucket newBucket = new Bucket(bucket);
    newBucket.inputType = IOType.FILE;
    newBucket.points = new ArrayList<GeoVector>(batchSize);
    newBucket.records = new ArrayList<String>(batchSize);
    newBucket.recordMap = new ArrayListInt(batchSize); 

    if (siteLatIndex >= 0)
      newBucket.receivers = new ArrayList<Receiver>(batchSize);

    if (phaseIndex >= 0)
      newBucket.phases = new ArrayList<SeismicPhase>(batchSize);

    if (jdateIndex >= 0)
      newBucket.time = new ArrayListDouble(batchSize);

    String line;
    Scanner scanner;
    int count = 0;
    while (input.hasNext() && ++count <= batchSize)
    {
      line = input.nextLine();
      newBucket.records.add(line);

      line = line.trim();

      try
      {
        if (line.startsWith("#") || line.length()==0)
          throw new Exception();

        scanner = new Scanner(line);
        String[] columns = new String[bucket.inputAttributes.size()];

        for (int i=0; i<bucket.inputAttributes.size(); ++i)
          columns[i] = scanner.next().trim();

        newBucket.points.add(new GeoVector(
            Double.parseDouble(columns[sourceLatIndex]),
            Double.parseDouble(columns[sourceLonIndex]),
            sourceDepthIndex < 0 ? Double.NaN : Double.parseDouble(columns[sourceDepthIndex]), 
                true));

        if (jdateIndex >= 0)
          newBucket.time.add(GMTFormat.getEpochTime(Integer.parseInt(columns[jdateIndex])));

        if (siteLatIndex >= 0)
        {
        	Receiver r = new Receiver(
                    staIndex < 0 ? newBucket.site.getSta() : columns[staIndex],
                            new GeoVector(
                                Double.parseDouble(columns[siteLatIndex]),
                                Double.parseDouble(columns[siteLonIndex]),
                                sign*Double.parseDouble(columns[siteDepthIndex]),	true));
        	if (siteOndateIndex >= 0)
        		r.setOndate(Integer.parseInt(columns[siteOndateIndex]));
        	else
        		r.setOndate(-1);
          newBucket.receivers.add(r);
        }

        if (phaseIndex >= 0)
          newBucket.phases.add(SeismicPhase.valueOf(columns[phaseIndex]));

        // add entry to map that relates the observationId(predictionRequest index)
        // with current record index so that later, results can be appended onto 
        // the appropriate record.
        newBucket.recordMap.add(newBucket.points.size()-1);
      } 
      catch (Exception ex)
      {
        // if any exceptions happen simply ignore the line.  The record will still
        // be recorded in bucket.records, but no predictions will be computed.
        newBucket.recordMap.add(-1);
      }
    }

    if (log.isOutputOn())
      log.writeln(String.format("DataSourceFile.next() produced %d records.", 
          newBucket.points.size()));

    moreData = input.hasNext();

    return newBucket;
  }

}
