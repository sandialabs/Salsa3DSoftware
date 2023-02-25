# PyPCalc.py
"""
Python wrapper to the PCalc java program
Requires Salsa3DSoftware jar file

This class is used to build property files that can then be used as arguments to pcalc

Note that we differentiate properties as values that go into the properties file
and configuration which refers to how we run PCalc

Created on Wed January 12 18:07:50 2022

@author: Rob Porritt

Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.

"""
import os
import math
import subprocess
import numpy as np
import pandas as pd

class PCalc():
    def __init__(self, properties = None, config = None, viewCopyRight=False):
        # First, we deal with properties.
        # These are the values that go into the properties file
        if properties is None:
            properties = self.initialize_properties()
        self.properties = properties

        # Now we check configuration
        # These refer to how the class calls the jar file
        if config is None:
            config = self.initialize_configuration()
        self.config = config

        if viewCopyRight:
            self.__viewCopyRight()

    @staticmethod
    def __viewCopyRight(viewFullLicense = True):
        print("PyPCalc Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.")
        print("\n")
        if viewFullLicense:
            PCalc.__viewFullLicense()
        print("Set viewCopyRight=False to supress this message.")
        return

    @staticmethod
    def __viewFullLicense():
        mystring = """

Copyright 2009 Sandia Corporation. Under the terms of Contract
DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
retains certain rights in this software.

BSD Open Source License.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   * Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
   * Neither the name of Sandia National Laboratories nor the names of its
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
"""
        print(mystring)
        return

    @staticmethod
    def initialize_configuration(jarFile = None,
                                propertiesFile = "PCalc.properties", captureOutput="True",
                                slbm_libdir = None,
                                ojdbc_jar = None,
                                use_slbm = False, use_oracle = False,
                                returnObject='array',
                                executionMode = 'Salsa3DSoftware',
                                wallet = None, memoryString="-Xmx256g"
                                ):
        config = {
            "jarFile": jarFile,
            "propertiesFile": propertiesFile,
            "captureOutput": captureOutput,
            "propertyFileWritten": False,
            "slbm_libdir": slbm_libdir,
            "use_slbm": use_slbm,
            "use_oracle": use_oracle,
            "ojdbc_jar": ojdbc_jar,
            "returnObject": returnObject,
            "executionMode": executionMode,
            "wallet": wallet,
            "memoryString": memoryString
            
        }
        return config


    @staticmethod
    def initialize_properties(application=None, workDir = '.',
                              geotessModel = None,
                              inputType=None, inputAttributes = None, batchSize= 1,
                              outputFile = None,
                              logFile= "<property:workDir>/pcalc_log.txt",
                              terminalOutput = True, separator = "space", outputAttributes = None,
                              inputFile = None, outputHeader=True,
                              gcStart = None, gcDistance = None, gcAzimuth = None, gcSpacing = None,
                              depthSpecificationMethod=None,
                              gcPositionParameters = None, maxDepthSpacing = None, maxDepth = None,
                              gridRangeLat = None, gridRangeLon = None, depths = None, outputFormat = "%1.4f",
                              predictors = None, benderModel = None,
                              phase = None, site = None, sta = None, jdate=None,
                              maxProcessors=None, depthLevels = None, slbmModel = None,
                              dbInputInstance = None, dbInputUserName = None, dbInputPassword = None,
                              dbInputTablePrefix = None, dbInputTableTypes = None, dbInputSiteTable = None,
                              dbInputWhereClause = None, outputType = None, dbOutputInstance = None,
                              dbOutputUserName = None, dbOutputPassword = None, dbOutputAssocTable = None,
                              dbOutputAutoTableCreation = None, dbOutputPromptBeforeTruncate = None,
                              dbOutputTruncateTables = None,
                              gcNpoints = None, gcOnCenters = None, rayPathNodeSpacing = None,
                              parallelMode = None, benderUncertaintyType = None, benderUncertaintyDirectory = None,
                              benderUncertaintyWorkDir = None, benderAllowCMBDiffraction = None,
                              benderAllowMOHODiffraction = None, supportedPhases = None,
                              geotessDataType = None, geotessRotateGridToStation = None, geotessBaseEdgeLengths = None,
                              geotessActiveNodeRadius = None, geotessPolygons = None, geotessDepths = None,
                              spanSeismicityDepth = None, overwriteExistingOutputFile = None, gcEnd=None
                             ):
        assert application in ['model_query','predictions'], 'Error, application must be model_query or predictions'
        assert inputType in ['file', 'greatcircle', 'grid', 'database'], 'Error, inputType must be file, greatcircle, database, or grid'
        assert separator in ['tab', 'space', 'comma'], 'Error, separator must be tab, space, or comma'
        assert depthSpecificationMethod in [None, 'maxDepthSpacing', 'depths', 'depthRange', 'depthLevels'], 'Error, depthSpecificationMethod must be maxDepthSpacing, depths, depthRange, or depthLevels'

        props = {
            "application": application,
            "workDir": workDir,
            "geotessModel": geotessModel,
            "inputType": inputType,
            "inputAttributes": inputAttributes,
            "batchSize": batchSize,
            "outputFile": outputFile,
            "logFile": logFile,
            "terminalOutput": terminalOutput,
            "separator": separator,
            "outputAttributes": outputAttributes,
            "inputFile": inputFile,
            "outputHeader": outputHeader,
            "gcStart": gcStart,
            "gcDistance": gcDistance,
            "gcAzimuth": gcAzimuth,
            "gcSpacing": gcSpacing,
            "gcEnd": gcEnd,
            "gcPositionParameters": gcPositionParameters,
            "depthSpecificationMethod": depthSpecificationMethod,
            "maxDepthSpacing": maxDepthSpacing,
            "maxDepth": maxDepth,
            "gridRangeLat": gridRangeLat,
            "gridRangeLon": gridRangeLon,
            "depths": depths,
            "outputFormat": outputFormat,
            "predictors": predictors,
            "benderModel": benderModel,
            "phase": phase,
            "site": site,
            "sta": sta,
            "jdate": jdate,
            "maxProcessors": maxProcessors,
            "depthLevels": depthLevels,
            "slbmModel": slbmModel,
            "dbInputInstance": dbInputInstance,
            "dbInputUserName": dbInputUserName,
            "dbInputPassword": dbInputPassword,
            "dbInputTablePrefix": dbInputTablePrefix,
            "dbInputTableTypes": dbInputTableTypes,
            "dbInputSiteTable": dbInputSiteTable,
            "dbInputWhereClause": dbInputWhereClause,
            "outputType": outputType,
            "dbOutputInstance": dbOutputInstance,
            "dbOutputUserName": dbOutputUserName,
            "dbOutputPassword": dbOutputPassword,
            "dbOutputAssocTable": dbOutputAssocTable,
            "dbOutputAutoTableCreation": dbOutputAutoTableCreation,
            "dbOutputPromptBeforeTruncate": dbOutputPromptBeforeTruncate,
            "dbOutputTruncateTables": dbOutputTruncateTables,
            "gcNpoints": gcNpoints,
            "gcOnCenters": gcOnCenters,
            "rayPathNodeSpacing": rayPathNodeSpacing,
            "parallelMode": parallelMode,
            "benderUncertaintyType": benderUncertaintyType,
            "benderUncertaintyDirectory": benderUncertaintyDirectory,
            "benderUncertaintyWorkDir":benderUncertaintyWorkDir,
            "benderAllowCMBDiffraction": benderAllowCMBDiffraction,
            "benderAllowMOHODiffraction": benderAllowMOHODiffraction,
            "supportedPhases": supportedPhases,
            "geotessDataType": geotessDataType,
            "geotessRotateGridToStation": geotessRotateGridToStation,
            "geotessBaseEdgeLengths": geotessBaseEdgeLengths,
            "geotessActiveNodeRadius": geotessActiveNodeRadius,
            "geotessPolygons": geotessPolygons,
            "geotessDepths": geotessDepths,
            "spanSeismicityDepth": spanSeismicityDepth,
            "overwriteExistingOutputFile": overwriteExistingOutputFile
        }
        return props

    def execute(self):
        data = None
        cstrings = []
        if self.config['executionMode'] == 'original':
            st1 = 'java'
            cstrings.append(st1)
            st2 = '-cp'
            cstrings.append(st2)
            if self.config['use_oracle']:
                st3 = self.config['jarfile'] + ":" + self.config['ojdbc_jar']
            else:
                st3 = self.config['jarFile']
            cstrings.append(st3)
            st4 = 'gov.sandia.gmp.pcalc.PCalc'
            cstrings.append(st4)
            st5 = self.config['propertiesFile']
            cstrings.append(st5)
        else:
            st1 = 'java'
            cstrings.append(st1)
            if self.config['memoryString'] is not None:
                cstrings.append(self.config['memoryString'])
            st3 = '-classpath'
            cstrings.append(st3)
            if self.config['use_oracle']:
                st4 = self.config['jarfile'] + ":" + self.config['ojdbc_jar']
            else:
                st4 = self.config['jarFile']
            cstrings.append(st4)
            if self.config['wallet'] is not None:
                cstrings.append("-Doracle.net.wallet_location={}".format(self.config['wallet']))
                cstrings.append("-Doracle.net.tns_admin={}".format(self.config['wallet']))
            cstrings.append("gov.sandia.gmp.pcalc.PCalc")
            cstrings.append(self.config['propertiesFile'])
        if self.config['use_slbm']:
            st6 = "-Djava.library.path={}".format(self.config['slbm_libdir'])
            cstrings.append(st6)
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
            except:
                print("Error, unable to run PCalc (slbm mode).")
                return -1
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
            except:
                print("Error running PCalc")
                return -2
        # Now we get the output and include it with the result
        data = self.getOutput()

        return [result, data]

    def getOutput(self):
        """
        Wrapper to feed getOutputFile, getOutputGreatCircle, or getOutputGrid
        Note that these different methods are static meaning they can be called without making a class object
        """
        nattributes = len(self.properties['outputAttributes'].split(" "))
        separator = self.properties['separator']
        fname = self.getOutputFileName()
        if self.properties['outputHeader']:
            header = 0
        else:
            header = None

        # Need to add a mode related to ray paths
        if self.properties['outputAttributes'].lower() == 'ray_path':
            if self.config['returnObject'].lower() != 'points':
                print("Warning, ray path return object can only be Points")
            coords = self.properties['gcPositionParameters'].split(" ")
            ilon, ilat, idep, irad, ix, iy, iz, idist = self.check_coords(coords)
            result = self.getOutputRayPath(fname, separator=separator, header=header, ilat=ilat, ilon=ilon, idepth=idep, irad=irad, ix=ix, iy=iy, iz=iz, idist=idist)
        elif self.properties['inputType'] == 'file':
            # determine which column maps to which spatial coordinate
            ilon = 0
            ilat = 0
            idep = 0
            coords = self.properties['inputAttributes'].split(" ")
            a=coords[0].lower().replace(",","").strip()
            b=coords[1].lower().replace(",","").strip()
            c=coords[2].lower().replace(",","").strip()
            coords[0] = a
            coords[1] = b
            coords[2] = c
            if coords[0] == 'latitude' or coords[0]== 'origin_lat':
                ilat = 0
            elif coords[0] == 'longitude' or coords[0] == 'origin_lon':
                ilon = 0
            elif coords[0] == 'depth' or coords[0] == 'radius' or coords[0] == 'origin_depth':
                idep = 0
            if coords[1] == 'latitude' or coords[1] == 'origin_lat':
                ilat = 1
            elif coords[1] == 'longitude' or coords[1] == 'origin_lon':
                ilon = 1
            elif coords[1] == 'depth' or coords[1] == 'radius' or coords[1] == 'origin_depth':
                idep = 1
            if coords[2] == 'latitude' or coords[2] == 'origin_lat':
                ilat = 2
            elif coords[2] == 'longitude' or coords[2] == 'origin_lon':
                ilon = 2
            elif coords[2] == 'depth' or coords[2] == 'radius' or coords[2] == 'origin_depth':
                idep = 2
            # Call the reading method
            result = self.getOutputFile(fname, separator=separator, nattributes=nattributes, ilat=ilat, ilon=ilon, idep=idep, header=header, returnObject=self.config['returnObject'])
        elif self.properties['inputType'] == 'greatcircle':
            coords = self.properties['gcPositionParameters'].split(" ")
            ilon, ilat, idep, irad, ix, iy, iz, idist = self.check_coords(coords)
            result = self.getOutputGreatCircle(fname, separator=separator, nattributes=nattributes, header=header, returnObject=self.config['returnObject'], ilat=ilat, ilon=ilon, idepth=idep, irad=irad, ix=ix, iy=iy, iz=iz, idist=idist)
        elif self.properties['inputType'] == 'grid':
            result = self.getOutputGrid(fname, separator=separator, nattributes=nattributes, header=header, returnObject=self.config['returnObject'])
        else:
            print("Error, type not found.")
            result = -1
        return result

    @staticmethod
    def check_coords(labels):
        ilon, ilat, idep, irad, ix, iy, iz, idist = -1, -1, -1, -1, -1, -1, -1, -1
        for ilab, lab in enumerate(labels):
            a = lab.lower().replace(",","").strip()
            if a  == 'longitude' or a  == 'lon':
                ilon = ilab
            if a  == 'latitude' or a  == 'lat':
                ilat = ilab
            if a  == 'depth':
                idep = ilab
            if a  == 'radius':
                irad = ilab
            if a  == 'x':
                ix = ilab
            if a  == 'y':
                iy = ilab
            if a  == 'z':
                iz = ilab
            if a  == 'gcdistance' or a == 'distance':
                idist = ilab
        return ilon, ilat, idep, irad, ix, iy, iz, idist

    @staticmethod
    def getOutputFile(filename, separator = "space", nattributes = 1, returnObject = 'array', ilat=0, ilon=0, idep=0, header=0):
        """
        This file basically mirrors the input having columns for x-y-z locations and then attributes
        It may also contain a header line that will be parsed
        Uses pandas to simplify the parsing.
        returnObject specifics if we should return a numpy ndarray (array), dataFrame (dataFrame), or points (points)
        dataFrame is the default object formed when using pandas.read_csv and therefore comes packed with methods
        array uses the inputAttributes to determine order of columns and will always re-organize so output is x, y, z, value(s)
        points creates an array of dictionary
        """
        assert returnObject in ['array', 'dataFrame', 'points'], 'Error, returnObject must be array, dataFrame, or points'
        if separator == 'space':
            delimiter = ' '
        elif separator == 'tab':
            delimiter = '\t'
        elif separator == 'comma':
            delimiter = ','

        data = pd.read_csv(filename, delimiter = delimiter, header = header, skipinitialspace=True)
        # Here we separate giving the data frame object directly back to the caller or parse it out for them
        if returnObject == "dataFrame":
            return data

        dataArray = np.asarray(data)
        if returnObject == 'points':
            dataFormatted = []
            for ipt, _ in enumerate(dataArray[:,0]):
                att = np.zeros((nattributes,))
                for iatt in range(nattributes):
                    att[iatt] = dataArray[ipt, 3+iatt]
                tmp = PointMeasurement(longitude=dataArray[ipt,ilon], latitude=dataArray[ipt,ilat], depth=dataArray[ipt, idep], attributes=att)
                dataFormatted.append(tmp)
            return dataFormatted

        if returnObject == 'array':
            dataFormatted = np.zeros((dataArray.shape))
            dataFormatted[:,0] = dataArray[:, ilon]
            dataFormatted[:,1] = dataArray[:, ilat]
            dataFormatted[:,2] = dataArray[:, idep]
            for iatt in range(nattributes):
                dataFormatted[:, 3+iatt] = dataArray[:, 3+iatt]
            return dataFormatted

        # Should not get here, so returning an error code
        return -1

    @staticmethod
    def getOutputGreatCircle(filename, separator = "space", nattributes = 1, returnObject='array', ilat=-1, ilon=-1, idist=-1, idepth=-1, irad=-1, ix=-1, iy=-1, iz=-1, header=0):
        """
        Little more complicated as this mode can return variable outputs:
        latitude, longitude, distance, depth, radius, x, y, z
        Set column input to -1 to turn off any of these outputs (default)
        For the array case, this just calls numpy.asarray on the dataframe, assuming the user knows the order
        and arrays set in gcPositionParameters
        """
        assert returnObject in ['array', 'dataFrame', 'points'], 'Error, returnObject must be array, dataFrame, or points'
        if separator == 'space':
            delimiter = ' '
        elif separator == 'tab':
            delimiter = '\t'
        elif separator == 'comma':
            delimiter = ','

        data = pd.read_csv(filename, delimiter = delimiter, header = header, skipinitialspace=True)
        if returnObject == "dataFrame":
            return data

        dataArray = np.asarray(data)
        nloc = 0
        if ilat >= 0:
            nloc += 1
        if ilon >= 0:
            nloc += 1
        if idist >= 0:
            nloc += 1
        if idepth >= 0:
            nloc += 1
        if irad >= 0:
            nloc += 1
        if ix >= 0:
            nloc += 1
        if iy >= 0:
            nloc += 1
        if iz >= 0:
            nloc += 1

        if returnObject == 'points':
            dataFormatted = []
            for ipt, _ in enumerate(dataArray[:,0]):
                att = np.zeros((nattributes,))
                for iatt in range(nattributes):
                    att[iatt] = dataArray[ipt, nloc+iatt]
                if ilat >= 0:
                    lat = dataArray[ipt, ilat]
                else:
                    lat = None
                if ilon >= 0:
                    lon = dataArray[ipt, ilon]
                else:
                    lon = None
                if idist >= 0:
                    dist = dataArray[ipt, idist]
                else:
                    dist = None
                if idepth >= 0:
                    dep = dataArray[ipt, idepth]
                else:
                    dep = None
                if irad >= 0:
                    rad = dataArray[ipt, irad]
                else:
                    rad = None
                if ix >= 0:
                    xtmp = dataArray[ipt, ix]
                else:
                    xtmp = None
                if iy >= 0:
                    ytmp = dataArray[ipt, iy]
                else:
                    ytmp = None
                if iz >= 0:
                    ztmp = dataArray[ipt, iz]
                else:
                    ztmp = None

                tmp = PointMeasurement(longitude=lon, latitude=lat, depth=dep, radius=rad, x=xtmp, y=ytmp, z=ztmp, gcDistance=dist, attributes=att)
                dataFormatted.append(tmp)
            return dataFormatted

        if returnObject == 'array':
            dataFormatted = np.asarray(dataArray)
            return dataFormatted

        return -1

    @staticmethod
    def getOutputRayPath(filename, separator = "space", ilat=-1, ilon=-1, idist=-1, idepth=-1, irad=-1, ix=-1, iy=-1, iz=-1, header=0):
        """
        Based on getOutputGreatCircle, but the file to be scanned contains ">" symbols to break up rays
        However, each ray has the same basic structure as a greatcircle output
        Output is the PointMeasurement object, but the various elements (latitude, longitude, etc...) are vectors.
        """
        if separator == 'space':
            sep = ' '
        elif separator == 'tab':
            sep = '\t'
        elif separator == 'comma':
            sep = ','

        # Reads the file and puts the raw data into 'all_lines' list of strings
        fp = open(filename, 'r')
        all_lines = fp.readlines()
        fp.close()

        # determines how many header lines to read.
        # note that header should either be "0" or "None" to align with other getOutput methods
        if header == 0:
            nheader = 1
        else:
            nheader = 0

        # String that separates individual rays
        nextRay = ">"

        nrays = 0
        rays = []
        for iline, line in enumerate(all_lines):
            if iline >= nheader:
                if line.strip() == nextRay:
                    if nrays > 0:
                        tmp = PointMeasurement(latitude = np.asarray(currentRayLat,dtype='float'),
                                              longitude = np.asarray(currentRayLon,dtype='float'),
                                              depth = np.asarray(currentRayDep,dtype='float'),
                                              gcDistance = np.asarray(currentRayDistance,dtype='float'),
                                              x = np.asarray(currentRayX, dtype='float'),
                                              y = np.asarray(currentRayY, dtype='float'),
                                              z = np.asarray(currentRayZ, dtype='float'),
                                              radius = np.asarray(currentRayRad, dtype='float'))
                        rays.append(tmp)
                    nrays += 1
                    currentRayLat = []
                    currentRayLon = []
                    currentRayDep = []
                    currentRayRad = []
                    currentRayX = []
                    currentRayY = []
                    currentRayZ = []
                    currentRayDistance = []
                else:
                    tmpstr = line.split(sep)
                    tmpstr[-1] = tmpstr[-1].strip()
                    if ilat >= 0:
                        currentRayLat.append(tmpstr[ilat])
                    if ilon >= 0:
                        currentRayLon.append(tmpstr[ilon])
                    if idist >= 0:
                        currentRayDistance.append(tmpstr[idist])
                    if idepth >= 0:
                        currentRayDep.append(tmpstr[idepth])
                    if ix >= 0:
                        currentRayX.append(tmpstr[ix])
                    if iy >= 0:
                        currentRayY.append(tmpstr[iy])
                    if iz >= 0:
                        currentRayZ.append(tmpstr[iz])
                    if irad >= 0:
                        currentRayRad.append(tmpstr[irad])

        tmp = PointMeasurement(latitude = np.asarray(currentRayLat,dtype='float'),
                                              longitude = np.asarray(currentRayLon,dtype='float'),
                                              depth = np.asarray(currentRayDep,dtype='float'),
                                              gcDistance = np.asarray(currentRayDistance,dtype='float'),
                                              x = np.asarray(currentRayX, dtype='float'),
                                              y = np.asarray(currentRayY, dtype='float'),
                                              z = np.asarray(currentRayZ, dtype='float'),
                                              radius = np.asarray(currentRayRad, dtype='float'))
        rays.append(tmp)
        return rays

    @staticmethod
    def getOutputGrid(filename, separator = "space", nattributes = 1, returnObject='array', header=0):
        """
        """
        assert returnObject in ['array', 'dataFrame', 'points'], 'Error, returnObject must be array, dataFrame, or points'
        if separator == 'space':
            delimiter = ' '
        elif separator == 'tab':
            delimiter = '\t'
        elif separator == 'comma':
            delimiter = ','

        data = pd.read_csv(filename, delimiter = delimiter, header = header, skipinitialspace=True)
        if returnObject == "dataFrame":
            return data
        elif returnObject == 'array':
            return np.asarray(data)
        elif returnObject == 'points':
            dataFormatted = []
            dataArray = np.asarray(data)
            for ipt, _ in enumerate(dataArray[:,0]):
                att = np.zeros((nattributes,))
                for iatt in range(nattributes):
                    att[iatt] = dataArray[ipt, 3+iatt]
                tmp = PointMeasurement(longitude=dataArray[ipt,0], latitude=dataArray[ipt,1], depth=dataArray[ipt, 2], attributes=att)
                dataFormatted.append(tmp)
            return dataFormatted

        return -1


    def checkPropertyLead(self, string):
        """
        Checks if a given property string begins with another property
        This is as per examples
        """
        if "<property:" in string:
            ind = string.index(">")
            prop = string[10:ind]
            propertyInfo = self.properties[prop]
            return [propertyInfo, string[ind+1:]]
        else:
            return None

    def getOutputFileName(self):
        fname = self.checkPropertyLead(self.properties['outputFile'])
        if fname is None:
            return self.properties['outputFile']
        else:
            return os.sep.join([fname[0], fname[1]])

    def writePropertiesFile(self):
        """
        Writes the property file to be used during execution
        """
        self.check_properties()
        with open(self.config['propertiesFile'], 'w') as propertyFile:
            for prop in self.properties:
                if self.properties[prop] is not None:
                    propertyFile.write("{} = {}\n".format(prop, self.properties[prop]))
        self.config['propertyFileWritten'] = True
        return

    @staticmethod
    def read_pcalc_properties_file(fname):
        """
        Reads a properties file designed for pcalc and returns properties object
        """

        # Initialize a properties dictionary to be overwritten
        props = PCalc.initialize_properties(application="model_query",
            inputType="file", separator="space", depthSpecificationMethod = None)

        propertyFile = open(fname, 'r')
        lines = propertyFile.readlines()
        for idx, line in enumerate(lines):
            line_parts = line.split(" ")
            prop = line_parts[0]
            val = line_parts[2:]
            valstr = ""
            for v in val:
                valstr += str(v)
                valstr += " "
            props[prop] = valstr.split("\n")[0]
        return props

    def check_properties(self):
        """
        Not fully implemented method to check that the user passed in acceptable parameters
        """
        if self.properties['outputAttributes'].lower() == 'ray_path':
            pass
        pass

    def setJarFile(self, jarfile=None):
        self.config['jarFile'] = jarfile
        return

    @staticmethod
    def print_example01_info():
        print("")
        print("Example 1 showcases the model query functionality of PCalc.")
        print("Given a GeoTess formatted model with known attributes PCalc will get the value in the model at the point specificed.")
        print("Point specification is defined in a file with coordinates given in longitude latitude and depth")
        print("")
        print("Set properties include:")
        print("application = model_query")
        print("workDir = /Users/username/pcalc/Examples/Example01/")
        print("geotessModel = <property:workDir>../data/AK135.geotess")
        print("inputFile = <property:workDir>../data/example_coords.xyz")
        print("inputType = file")
        print("inputAttributes = latitude longitude depth")
        print("batchSize = 10")
        print("outputFile = <property:workDir>/pcalc_query_file_output.dat")
        print("logFile = <property:workDir>/pcalc_log.txt")
        print("terminalOutput = true")
        print("separator = space")
        print("outputAttributes = pslowness")
        print("")
        return

    @staticmethod
    def print_example02_info():
        print("")
        print("Example 2 showcases the great circle output functionality of PCalc.")
        print("This mode is designed to prepare output in a format that can be used in a cross-section.")
        print("key properties include:")
        print("inputType='greatcircle")
        print("gcStart = '0 0' # defines the latitude and longitude of the start of the circle")
        print("gcDistance = 180 # defines the distance, in degrees, of the circle")
        print("gcAzimuth = 0 # defines the azimuth of the circle")
        print("gcSpacing = 10 # distance, in degrees, between points along the circle")
        print("gcPositionParameters = 'x, y, distance, depth' # string consisting of positions output by gc mode. Options include: longitude, latitude, depth, distance, radius, x, y, and z. Each can appear once and only once, but in any order.")
        print("depthSpecificationMethod = 'maxDepthSpacing' # defines how the depth axis is to be defined. Choose one of 'depths', 'depthRange', depthLevels', or 'maxDepthSpacing'. Each mode requires additional specifications.")
        print("maxDepthSpacing = 100 # Maximum distance between two points in depth")
        print("maxDepth =  'top of m660' # A definition of the maximum depth. Can either reference a layer or be a static value.")
        print("outputHeader = True")
        print("")
        return

    @staticmethod
    def print_example03_info():
        print("")
        print("Example 3 showcases the output grid functionality")
        print("Key properties include:")
        print("inputType = 'grid'")
        print("gridRangeLat = '15 45 16' # sets min, max, and N latitudes")
        print("gridRangeLon = '70 110 21' # sets min, max and N longitudes")
        print("depthSpecificationMethod = 'depths' # see ex2 or the manual for more on depth specification")
        print("depths = '100.0, 200.0' # Set of depths to compute the grids in lat/lon for")
        print("")
        return

    @staticmethod
    def print_example04_info():
        print("")
        print("Example 4 returns to the file inputType, but now uses bender to predict travel times.")
        print("key properties include:")
        print("maxProcessors = 4 # Allows parallelization")
        print("application = 'predictions'")
        print("predictors = 'bender'")
        print("benderModel = '<property:workDir>../data/AK135.geotess'")
        print("inputAttributes = 'origin_lat, origin_lon, origin_depth'")
        print("phase = 'P'")
        print("site = '37, 139, 0.6'")
        print("sta = 'SYNTH'")
        print("jdate = '2011001'")
        print("outputAttributes = 'travel_time'")
        print("")
        return

    @staticmethod
    def print_example05_info():
        print("")
        print("Example 5 shows the predictions application, but for a great circle")
        print("No new properties are set here, but note that this uses the built-in 1D model, ak135")
        print("  for the wavespeed info by setting predictors = lookup2d")
        print("")
        return

    @staticmethod
    def print_example06_info():
        print("")
        print("Example 6 uses the predictions method on a grid.")
        print("")
        return

    @staticmethod
    def print_example07_info():
        print("")
        print("Example 7 uses an oracle database of CSS3.0 schema tables to read and write data.")
        print("This uses several properties to define the database")
        print("inputType = 'database'")
        print("dbInputInstance = 'jdbc:oracle:thin:@domain:port:database'")
        print("dbInputUserName = 'username'")
        print("dbInputPassword = 'password'")
        print("dbInputTablePrefix = 'uebgt_'")
        print("dbInputTableTypes = 'origin, arrival, assoc'")
        print("dbInputSiteTable = 'uebgt_site'")
        print("dbInputWhereClause = 'origin.orid = 48834027'")
        print("outputType = 'database'")
        print("dbOutputInstance = '<property: dbInputInstance>'")
        print("dbOutputUserName = '<property: dbInputUserName>'")
        print("dbOutputPassword = '<property: dbInputPassword>'")
        print("dbOutputAssocTable = 'pcalc_assoc'")
        print("dbOutputAutoTableCreation = True")
        print("dbOutputPromptBeforeTruncate = False")
        print("dbOutputTruncateTables = True")
        print("")
        print("In the manual, this also illustrates using the RSTT or SLBM velocity model:")
        print("predictors = 'slbm'")
        print("slbmModel = '/Users/username/pcalc_software/pdu202009DU.geotess'")
        print("")
        return

    @staticmethod
    def print_example08_info():
        print("")
        print("Example 8 illustrates generating ray paths using great circles.")
        print("key properties:")
        print("inputType = 'greatcircle'")
        print("gcNpoints = 19")
        print("gcOnCenters = False")
        print("outputAttributes = 'ray_path'")
        print("outputType = 'file'")
        print("")
        return


    # Probably going to remove this as it would just get too big and wonky
    @staticmethod
    def listProperties():
        print("application <string> [Default = none] ( model_query | predictions )")
        print("Specifies the application that PCalc is to perform. The model_query application specifies that PCalc will extract requested data or metadata from an input model. The predictions application specifies that PCalc will generate travel-time or raypath predictions using input locations. Locations can be input as a file, grid, or great circle, respectively.")
        print("--------------------------------------")
        print("workDir <string> [Default = null: no text output]")
        print("Specifies the application that PCalc is to perform. The model_query application specifies that PCalc will extract requested data or metadata from an input model. The predictions application specifies that PCalc will generate travel-time or raypath predictions using input locations. Locations can be input as a file, grid, or great circle, respectively.")
        print("--------------------------------------")
        print("logFile <string> [Default = null: no text output]")
        print("Full path to log file. General information about the PCalc run is sent to this file. If property terminalOutput = true, the same information is sent to the screen.")
        print("--------------------------------------")
        print("terminalOutput <boolean> [Default = True]")
        print("Echo general information about the PCalc run. This is the same information that is sent to the log file. If false, PCalc is silent.")
        print("--------------------------------------")
        print("inputType <string> [Default = none] (file | database | greatcircle | grid | geotess)")
        print("String indicating how the geometry of the predictions / model queries is to be specified. This document contains a section for each inputType that describes the properties that are pertinent to that inputType.")
        print("--------------------------------------")
        print("sta <String> [no Default]")
        print("The name of the station. If sta and jdate are supplied then Bender will include tt_site_corrections in total travel times, regardless of whether tt_site_corrections is one of the requested outputAttributes or not.")
        print("--------------------------------------")

        return

    def viewSetProperties(self):
        for prop in self.properties:
            if self.properties[prop] is not None:
                print("{} = {}".format(prop, self.properties[prop]))
        return

    def viewSetConfiguration(self):
        for conf in self.config:
            print("{} = {}".format(conf, self.config[conf]))
        return


class PointMeasurement():
    def __init__(self, longitude=None, latitude=None, depth=None, radius=None, attributes = None, gcDistance=None, x=None, y=None, z=None):
        self.longitude = longitude
        self.latitude = latitude
        self.depth = depth
        if radius is None:
            if depth is not None:
                self.radius = 6371-depth
            else:
                self.radius = None
        else:
            self.radius = radius
        self.attributes = attributes
        # Values added for great circle mode
        self.gcDistance = gcDistance
        self.x = x
        self.y = y
        self.z = z
        return

    def __repr__(self):
        return "longitude: {}, latitude: {}, depth: {}, radius: {}, x: {}, y: {}, z: {}, gcDistance: {}, attributes: {}\n".format(self.longitude, self.latitude, self.depth, self.radius,  self.x, self.y, self.z,  self.gcDistance, self.attributes)

    def __str__(self):
        return "longitude: {}, latitude: {}, depth: {}, radius: {}, x: {}, y: {}, z: {}, gcDistance: {}, attributes: {}".format(self.longitude, self.latitude, self.depth, self.radius,  self.x, self.y, self.z,  self.gcDistance, self.attributes)

    def toString(self):
        print("Longitude: {}".format(self.longitude))
        print("Latitude: {}".format(self.latitude))
        print("Depth: {}".format(self.depth))
        print("Radius: {}".format(self.radius))
        if self.attributes is not None:
            for iatt, att in enumerate(self.attributes):
                print("Attribute[{}]: {}".format(iatt, att))
        if self.gcDistance is not None:
            print("gcDistance: {}".format(self.gcDistance))
        if self.x is not None:
            print("x: {}".format(self.x))
        if self.y is not None:
            print("y: {}".format(self.y))
        if self.z is not None:
            print("z: {}".format(self.z))

        return

    def write(self, fp, mode='longitude latitude depth', separator='space'):
        """
        Writes the longitude, latitude, and depth values to a file pointer
        """
        assert separator in ['space', 'comma', 'tab'], "Error, unrecognized separator (space, comma, or tab)"
        assert mode in ['longitude latitude depth', 'latitude longitude depth'], "Error, mode must be 'longitude latitude depth' or 'latitude longitude depth'"
        if separator == 'space':
            sep = ' '
        elif separator == 'comma':
            sep = ','
        elif separator == 'tab':
            sep = '\t'

        try:
            if mode == 'longitude latitude depth':
                fp.write("{}{}{}{}{}\n".format(self.longitude, sep, self.latitude, sep, self.depth))
            elif mode == 'latitude longitude depth':
                fp.write("{}{}{}{}{}\n".format(self.latitude, sep, self.longitude, sep, self.depth))

            return 0
        except:
            print("Error, file pointer not able to be written to.\n")
            return -1
        return 1
