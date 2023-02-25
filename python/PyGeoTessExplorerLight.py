"""
PyGeoTessExplorerLight.py

Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.

Thin wrapper to GeoTessExplorer functions in Salsa3DSoftware jar file

Rob Porritt, Sandia National Labs
February 24th, 2023

"""
import os
import math
import subprocess
import numpy as np
import pandas as pd


# Base class to organize call to jar file
class GeoTessExplorer:
    def __init__(self, config = None, viewCopyRight = False):
        if config is None:
            config = self.initialize_configuration()
        self.config = config
        if viewCopyRight:
            self.__viewCopyRight()

    def __str__(self):
        outstr = "Configuration: \n"
        for conf in self.config:
            outstr += str("{} = {}\n".format(conf, self.config[conf]))
        outstr += "\nUse the execute() method to run.\n"
        return outstr

    def __repr__(self):
        outstr = "Configuration: \n"
        for conf in self.config:
            outstr += str("{} = {}\n".format(conf, self.config[conf]))
        outstr += "\nUse the execute() method to run.\n"
        return outstr
    
    def _handle_error(self):
        print("Error, unable to run GeoTessExplorer.")
        result = ResultError(stdout="Run failed. See stderr", stderr="Failed to execute function: '{}'.".format(str(self.config['function'])))
        data = -1
        return [result, data]
        

    # Non-Pythonic method of displaying copy right info
    @staticmethod
    def __viewCopyRight():
        print("PyGeoTessExplorer Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.")
        print("\n")
        print("Set viewCopyRight=False to supress this message.")
        return

    # To allow users to set configuration before creating the object or to be called during initialization
    @staticmethod
    def initialize_configuration(jarFile = None,
                                 captureOutput="True",
                                 returnObject='array',
                                 function = "help",
                                 modelFile = None,
                                 gridPath = None,
                                 newModelFile = None,
                                 updatedDescription = None,
                                 secondModel = None,
                                 secondGridPath = None,
                                 gridOutputFile = None,
                                 gridLayTess = None,
                                 gridFileName = None,
                                 gridOutputMode = None,
                                 gridInputFile = None,
                                 newGrid = None,
                                 attributeIndices = None,
                                 reciprocal = False,
                                 polygon = None,
                                 newAttributeValuesFile = None,
                                 pointLatitude = None,
                                 pointLongitude = None,
                                 pointDepth = None,
                                 layerID = None,
                                 horizontalInterpolation = 'linear',
                                 radialInterpolation = 'linear',
                                 inputPointsFile = None,
                                 verticalOutputMode = 'radius',
                                 maximumRadialSpacing = 9001,
                                 deepestLayerID = None,
                                 shallowestLayerID = None,
                                 outputChoices = 'lat,lon,depth,radius,vertex,layer,node,point',
                                 beginLatitude = None,
                                 beginLongitude = None,
                                 endLatitude = None,
                                 endLongitude = None,
                                 shortestPath = True,
                                 nPointsSlice = 100,
                                 outputSpatialCoords = 'distance,depth,radius,x,y,z,lat,lon',
                                 degDistanceToLastPoint = 0,
                                 azimuthToLastPoint = 0,
                                 mapSpatialSamplingMode = 'number',
                                 nLongitudeMap = 100,
                                 nLatitudeMap = 100,
                                 spacingLatitudeMap = 0.5,
                                 spacingLongitudeMap = 0.5,
                                 fractionalRadius = 0.5,
                                 topOrBottomOfLayer = 'top',
                                 attributeIndex0 = 0,
                                 attributeIndex1 = 0,
                                 geometryModel = None,
                                 geometryModelPath = None,
                                 newAttributeName = None,
                                 newAttributeUnits = None,
                                 functionIndex = 0,
                                 mode = "execute",
                                 outputFileNameStub = None,
                                 layerIndices = None,
                                 vtkOutputFileName = None,
                                 firstDepth = None, lastDepth = None, depthSpacing = None,
                                 depths = None,
                                 depthOrElevation = 'depth',
                                 centerLongitude = None,
                                 radiusOutOfRangeAllowed = False,
                                 inputPolygon = None,
                                 outputPolygon = None,
                                 newSiteTermFile = None,
                                 RSTT_FileNameStub = None,
                                 RSTT_OutputType = None,
                                 oldLayerName = None,
                                 newLayerName = None,
                                 commonStringMode = 'salsa3dsoftware'
                                 ):
        config = {
            "jarFile": jarFile,
            "captureOutput": captureOutput,
            "returnObject": returnObject,
            "function": function,
            "modelFile": modelFile,
            "gridPath": gridPath,
            "newModelFile": newModelFile,
            "updatedDescription": updatedDescription,
            "secondModel": secondModel,
            "secondGridPath": secondGridPath,
            "gridOutputFile": gridOutputFile,
            "gridLayTess": gridLayTess,
            "gridFileName": gridFileName,
            "gridOutputMode": gridOutputMode,
            "gridInputFile": gridInputFile,
            "newGrid": newGrid,
            "attributeIndices": attributeIndices,
            "reciprocal": reciprocal,
            "polygon": polygon,
            "newAttributeValuesFile": newAttributeValuesFile,
            "pointLatitude": pointLatitude,
            "pointLongitude": pointLongitude,
            "pointDepth": pointDepth,
            "layerID": layerID,
            "horizontalInterpolation": horizontalInterpolation,
            "radialInterpolation": radialInterpolation,
            "inputPointsFile": inputPointsFile,
            "verticalOutputMode": verticalOutputMode,
            "maximumRadialSpacing": maximumRadialSpacing,
            "deepestLayerID": deepestLayerID,
            "shallowestLayerID": shallowestLayerID,
            "outputChoices": outputChoices,
            "beginLatitude": beginLatitude,
            "beginLongitude": beginLongitude,
            "endLatitude": endLatitude,
            "endLongitude": endLongitude,
            "shortestPath": shortestPath,
            "nPointsSlice": nPointsSlice,
            "outputSpatialCoords": outputSpatialCoords,
            "degDistanceToLastPoint": degDistanceToLastPoint,
            "azimuthToLastPoint": azimuthToLastPoint,
            "mapSpatialSamplingMode": mapSpatialSamplingMode,
            "nLongitudeMap": nLongitudeMap,
            "nLatitudeMap": nLatitudeMap,
            "spacingLatitudeMap": spacingLatitudeMap,
            "spacingLongitudeMap": spacingLongitudeMap,
            "fractionalRadius": fractionalRadius,
            "topOrBottomOfLayer": topOrBottomOfLayer,
            "attributeIndex0": attributeIndex0,
            "attributeIndex1": attributeIndex1,
            "geometryModel": geometryModel,
            "geometryModelPath": geometryModelPath,
            "newAttributeName": newAttributeName,
            "newAttributeUnits": newAttributeUnits,
            "functionIndex": functionIndex,
            "mode": mode,
            "outputFileNameStub": outputFileNameStub,
            "layerIndices": layerIndices,
            "vtkOutputFileName": vtkOutputFileName,
            "firstDepth": firstDepth,
            "lastDepth": lastDepth,
            "depthSpacing": depthSpacing,
            "depths": depths,
            "depthOrElevation": depthOrElevation,
            "centerLongitude": centerLongitude,
            "radiusOutOfRangeAllowed": radiusOutOfRangeAllowed,
            "inputPolygon": inputPolygon,
            "outputPolygon": outputPolygon,
            "newSiteTermFile": newSiteTermFile,
            "RSTT_FileNameStub": RSTT_FileNameStub,
            "RSTT_OutputType": RSTT_OutputType,
            "oldLayerName": oldLayerName,
            "newLayerName": newLayerName,
            "commonStringMode": commonStringMode
        }
        return config
    
    # Effectively the main method and a Rube-Goldbergian switch block to dictate how the jar file is called
    # Mainly sets up the arguments and gets output as .stdout, .stderr results messages
    # and data if available. If data is not available, returns an error code (single negative value). If function's
    # help is called, returns 0.
    def execute(self):
        """
        Method to call the geotessjava jar file with supplied configuration.
        returns two elements:
        result: a dictionary with stdout and stderr strings for results from the call.
        data: can be one of three types of output:
                a dataset returned by by the jar file if successful
                0 if the function's help usage is returned in stdout
                negative single value if there is an usage error. See result.stderr
        """
        # Initialize output
        data = None
        result = ResultError(stdout="None", stderr="None") 
        
        # common part of the calling string for subprocess
        st1 = 'java'
        st2 = '-jar'
        
        # Checks for jar file
        if self.config['jarFile'] is not None:
            st3 = self.config['jarFile']
        else:
            print("Error, must supply location of jar file.")
            result['stdout'] = 'Run failed. See stderr.'
            result['stderr'] = 'Jar file not supplied'
            data = -1
            return [result, data]
        
        function = self.config['function']
        assert function in ['help', 'version', 'toString', 'updateModelDescription','statistics',
        'getClassName','equal','extractGrid','resample','extractActiveNodes','replaceAttributeValues',
        'reformat','getValues','getValuesFile','interpolatePoint','borehole','profile','findClosestPoint',
        'slice','sliceDistAz','mapValuesDepth','mapValuesLayer','mapLayerBoundary','mapLayerThickness','values3DBlock',
        'function', 'vtkLayers', 'vtkDepths', 'vtkDepths2', 'vtkLayerThickness', 'vtkLayerBoundary','vtkSlice','vtkSolid',
        'vtk3DBlock', 'vtkPoints', 'vtkRobinson', 'vtkRobinsonLayers', 'vtkRobinsonPoints', 'vtkRobinsonTriangleSize',
        'vtkLayerAverage', 'reciprocalModel', 'renameLayer', 'getLatitudes', 'getLongitudes', 'getDistanceDegrees',
        'translatePolygon','extractSiteTerms', 'replaceSiteTerms', 'extractPathDependentUncertaintyRSTT',
        'replacePathDependentUncertaintyRSTT'], "Error, please check available functions via help"
        
        ##########################################################################
        # Main switch block for usage
        # Just the help (calling without any parameters)
        if self.config['function'].lower() in ["help", " "]:
            result, data = self._execute_help()
        elif self.config['mode'] in ['help', '-h', '?']:
            result, data = self._execute_help_function()
        # Returns the version of the GeoTessJava jar file
        elif self.config['function'].lower() in ['version']:
            result, data = self._execute_version()
        # toString, statistics, and getClassName all operate about the same way, requiring a modelFile
        elif self.config['function'].lower() in ['tostring']:
            result, data = self._execute_toString()
        elif self.config['function'].lower() in ['statistics']:
            result, data = self._execute_statistics()
        elif self.config['function'].lower() in ['getclassname']:
            result, data = self._execute_getClassName()
        # Update model description. Takes input model, output model, and new description
        elif self.config['function'].lower() in ['updatemodeldescription']:
            result, data = self._execute_updateModelDescription()
        elif self.config['function'].lower() in ['equal']:
            result, data = self._execute_equal()
        elif self.config['function'].lower() in ['extractgrid']:
            result, data = self._execute_extractGrid()
        elif self.config['function'].lower() in ['resample']:
            result, data = self._execute_resample()
        elif self.config['function'].lower() in ['extractactivenodes']:
            result, data = self._execute_extractActiveNodes()
        elif self.config['function'] == 'replaceAttributeValues':
            result, data = self._execute_replaceAttributeValues()
        elif self.config['function'] == 'reformat':
            result, data = self._execute_reformat()
        elif self.config['function'] in ['getValues']:
            result, data = self._execute_getValues()
        elif self.config['function'] in ['interpolatePoint']:
            result, data = self._execute_getValues() # note that this is identical to getValues in usage. Output is more verbose
        elif self.config['function'] == 'getValuesFile':
            result, data = self._execute_getValuesFile()
        elif self.config['function'] in ['borehole']:
            result, data = self._execute_borehole()
        elif self.config['function'] in ['profile']:
            result, data = self._execute_profile()
        elif self.config['function'] in ['findClosestPoint']:
            result, data = self._execute_findClosestPoint()
        elif self.config['function'] in ['slice']:
            result, data = self._execute_slice()
        elif self.config['function'] in ['sliceDistAz']:
            result, data = self._execute_sliceDistAz()
        elif self.config['function'] in ['mapValuesDepth', 'mapValuesLayer']:
            result, data = self._execute_mapValues()
        elif self.config['function'] in ['mapLayerBoundary']:
            result, data = self._execute_mapLayerBoundary()
        elif self.config['function'] in ['mapLayerThickness']:
            result, data = self._execute_mapLayerThickness()
        elif self.config['function'] in ['values3DBlock']:
            result, data = self._execute_values3DBlock()
        elif self.config['function'] in ['function']:
            result, data = self._execute_function()
        elif self.config['function'] in ['vtkLayers']:
            result, data = self._execute_vtkLayers()
        elif self.config['function'] in ['vtkDepths']:
            result, data = self._execute_vtkDepths()
        elif self.config['function'] in ['vtkDepths2']:
            result, data = self._execute_vtkDepths2()
        elif self.config['function'] in ['vtkLayerThickness']:
            result, data = self._execute_vtkLayerThickness()
        elif self.config['function'] in ['vtkLayerBoundary']:
            result, data = self._execute_vtkLayerBoundary()
        elif self.config['function'] in ['vtkSlice']:
            result, data = self._execute_vtkSlice()
        elif self.config['function'] in ['vtkSolid']:
            result, data = self._execute_vtkSolid()
        elif self.config['function'] in ['vtk3DBlock']:
            result, data = self._execute_vtk3DBlock()
        elif self.config['function'] in ['vtkPoints']:
            result, data = self._execute_vtkPoints()
        elif self.config['function'] in ['vtkRobinson']:
            result, data = self._execute_vtkRobinson()
        elif self.config['function'] in ['vtkRobinsonLayers']:
            result, data = self._execute_vtkRobinsonLayers()
        elif self.config['function'] in ['vtkRobinsonPoints']:
            result, data = self._execute_vtkRobinsonPoints()
        elif self.config['function'] in ['vtkRobinsonTriangleSize']:
            result, data = self._execute_vtkRobinsonTriangleSize()
        elif self.config['function'] in ['vtkLayerAverage']:
            result, data = self._execute_vtkLayerAverage()
        elif self.config['function'] in ['getLatitudes']:
            result, data = self._execute_getLatitudes()
        elif self.config['function'] in ['getLongitudes']:
            result, data = self._execute_getLongitudes()
        elif self.config['function'] in ['getDistanceDegrees']:
            result, data = self._execute_getDistanceDegrees()
        elif self.config['function'] in ['translatePolygon']:
            result, data = self._execute_translatePolygon()
        elif self.config['function'] in ['extractSiteTerms']:
            result, data = self._execute_extractSiteTerms()
        elif self.config['function'] in ['replaceSiteTerms']:
            result, data = self._execute_replaceSiteTerms()
        elif self.config['function'] in ['extractPathDependentUncertaintyRSTT']:
            result, data = self._execute_extractPathDependentUncertaintyRSTT()
        elif self.config['function'] in ['replacePathDependentUncertaintyRSTT']:
            result, data = self._execute_replacePathDependentUncertaintyRSTT()
        elif self.config['function'] in ['renameLayer']:
            result, data = self._execute_renameLayer()
            
        
        return [result, data]

    def getOutput(self, result):
        if self.config['gridOutputMode'] == 'stdout':
            grd = self.readGridStdout(result)
        elif self.config['gridOutputMode'] == 'gmt':
            grd = self.readGridGMT(result)
        elif self.config['function'] == 'extractActiveNodes':
            grd = self.getExtractActiveNodesOutput(result)
        elif self.config['function'] in ['getValues', 'findClosestPoint']:
            grd = self.getValuesOutput(result)
        elif self.config['function'] in ['getValuesFile', 'slice', 'sliceDistAz', 'mapValuesDepth', 'mapValuesLayer', 'mapLayerBoundary', 'mapLayerThickness', 'values3DBlock', 'profile', 'getLatitudes', 'getLongitudes', 'getDistanceDegrees']:
            grd = self.getValuesFileOutput(result)
        elif self.config['function'] == 'interpolatePoint':
            grd = self.getInterpolatePoint(result)
        elif self.config['function'] == 'borehole':
            grd = self.getBoreholeResults(result)
        elif self.config['function'] == 'extractSiteTerms':
            grd = self.getSiteTerms(result.stdout, mode='string')
        else:
            grd = "Not sure how this triggered."
        return grd
    
    def checkOutputChoices(self):
        if isinstance(self.config['outputChoices'], str):
            self.config['outputChoices'] = self.config['outputChoices'].replace(" ", "")
        else:
            tmp = ""
            for val in self.config['outputChoices']:
                if val in ['lon', 'lat', 'depth', 'radius' 'vertex', 'layer', 'node', 'point']:
                    if val != self.config['outputChoices'][-1]:
                        tmp += str(val) + ","
                    else:
                        tmp += str(val)
            self.config['outputChoices'] = tmp
        return
    
    def _common_strings(self, mode='salsa3dsoftware'):
        if mode == 'original':
            return ['java', '-jar', str(self.config['jarFile']), str(self.config['function']), str(self.config['modelFile'])]
        else:
            return ['java', '-cp', str(self.config['jarFile']), 'gov.sandia.geotess.GeoTessExplorer', str(self.config['function']), str(self.config['modelFile'])]
        
    def _execute_help(self):
        cstrings = self._common_strings(mode = self.config['commonStringMode'])
        print(cstrings)
        try:
            if len(cstrings) == 5:
                result = subprocess.run(cstrings[:3], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:4], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 1
        except:
            print("Error, unable to run GeoTessExplorer.")
            result = ResultError(stdout="Run failed. See stderr", stderr="Help failed to execute. Parameters = {}".format(cstrings))
            data = -2
        return [result, data]
    
    def _execute_version(self):
        cstrings = self._common_strings(mode = self.config['commonStringMode'])
        try:
            if len(cstrings) == 5:
                result = subprocess.run(cstrings[:4], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:5], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 1
        except:
            print("Error, unable to run GeoTessExplorer.")
            result = ResultError(stdout="Run failed. See stderr", stderr="Version failed to execute. Parameters = {}".format(cstrings))
            data = -3
        return [result, data]
    
    def _execute_help_function(self):
        cstrings = self._common_strings(mode = self.config['commonStringMode'])
        try:
            if len(cstrings) == 5:
                result = subprocess.run(cstrings[:4], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:5], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        except:
            print("Error, unable to run GeoTessExplorer.")
            result = ResultError(stdout="Run failed. See stderr", stderr="Help Function failed to execute. Parameters = {}".format(cstrings))
            data = -1
        return result, data
    
    def _execute_getClassName(self):
        cstrings = self._common_strings()
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        cstrings.append(st6)
        st5 = cstrings[-2]
        arglist = [st5, st6]
        if self.check_nones(arglist):
            print("  Requires: modelFile")
            print("  Optional: gridPath")
            print("  Returns: 1 on success, -1 on failure. ClassName is in result.stdout")
            if len(cstrings) == 6:
                result = subprocess.run(cstrings[:5], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:6], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="getClassName function error. Make sure a model is given.")
                data = -1
        return [result, data]
    
            
    def _execute_statistics(self):
        cstrings = self._common_strings()
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        cstrings.append(st6)
        st5 = cstrings[-2]
        arglist = [st5, st6]
        if self.check_nones(arglist):
            print("  Requires: modelFile")
            print("  Optional: gridPath")
            print("  Returns: data as Pandas DataFrame")
            if len(cstrings) == 6:
                result = subprocess.run(cstrings[:5], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:6], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.readStatisticsTable(result.stdout)
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="statistics function error. Make sure a model is given.")
                data = -1
        return [result, data]
    
    def _execute_toString(self):
        cstrings = self._common_strings()
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        cstrings.append(st6)
        st5 = cstrings[-2]
        arglist = [st5, st6]
        if self.check_nones(arglist):
            print("  Requires: modelFile")
            print("  Optional: gridPath")
            print("  Returns: 1 on success, -1 on failure. String is in result.stdout")
            if len(cstrings) == 6:
                result = subprocess.run(cstrings[:5], check=True, text=True, capture_output=self.config['captureOutput'])
            else:
                result = subprocess.run(cstrings[:6], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="toString function error. Make sure a model is given.")
                data = -1
        return [result, data]
    
    def _execute_updateModelDescription(self):
        cstrings = self._common_strings()
        st5 = cstrings[-1]
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st8 = self.config['newModelFile']
        st7 = self.config['updatedDescription']
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, newModelFile, updatedDescription")
            print("  Optional: None")
            print("  Returns: creates a new GeoTess model 'newModelFile' to disk. If successful, data return is 1.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
                data = -1
        return [result, data]
    
    def _execute_equal(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = self.config['secondModel']
        if self.config['secondGridPath'] is None:
            st8 = '.'
        else:
            st8 = self.config['secondGridPath']
        st5 = cstrings[-1]
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, secondModel")
            print("  Optional: gridPath, secondGridPath")
            print("  Returns: True or False boolean variable on success.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                rr = result.stdout.split("\n")
                if rr[0].split(" ")[0] == "equal":
                    data = True
                else:
                    data = False
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_extractGrid(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        if self.config['gridInputFile'] is None:
            st5 = str(self.config['modelFile'])
        else:
            st5 = str(self.config['gridInputFile'])
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = str(self.config['gridPath'])
        st7 = str(self.config['gridOutputFile'])
        st8 = str(self.config['gridLayTess'])
        arglist = [st5, st6, st7, st8]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: [modelFile or gridInputFile], gridOutputFile, gridLayTess")
            print("  Optional: gridPath")
            print("  Returns: if gridOutputFile is 'stdout', 'gmt' or ends in extension '.gmt', then the output is returned as npoints x 4 ndarray of triangle edge connections. Otherwise, a file is written to gridOutputFile and return value of success is 1.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                fname = self.config['gridOutputFile']
                ext = os.path.splitext(fname)[-1]
                if ext == ".gmt":
                    st7 = "gmt"
                    cstrings[-2] = st7
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                if self.config['gridOutputFile'] == 'stdout':
                    data = self.readGridStdout(result.stdout)
                elif self.config['gridOutputFile'] == 'gmt':
                    data = self.readGridGMT(result.stdout)
                elif ext == ".gmt":
                    data = self.readGridGMT(result.stdout, writeFile=True, outputFileName = fname)
                else:
                    data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_resample(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = self.config['newGrid']
        st8 = self.config['newModelFile']
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  This function resamples modelFile from its current grid onto newGrid and outputs as newModelFile")
            print("  Requires: modelFile, newGrid, newModelFile")
            print("  Optional: gridPath")
            print("  Returns: new file is written as newModelFile and data is set to 1 on return.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_extractActiveNodes(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        self.setAttributeIndices()
        st7 = self.config['attributeIndices']
        st8 = str(self.config['reciprocal'])
        if self.config['polygon'] is None:
            st9 = 'null'
        else:
            st9 = self.config['polygon']
        st5 = cstrings[-1]
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires configuration: modelFile, attributeIndices, reciprocal (True or False)")
            print("  Optional: gridPath, polygon (defaults to 'null')")
            print("  Returns: ndarray of npoints x 2 for a grid or npoints x nattributes for 2D or 3D model")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_replaceAttributeValues(self):
        cstrings = self._common_strings()
        st5 = cstrings[-1]
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        if self.config['polygon'] is None:
            st7 = 'null'
        else:
            st7 = self.config['polygon']
        st8 = self.config['newAttributeValuesFile']
        st9 = str(self.config['newModelFile'])
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires configuration values: modelFile, newAttributeValuesFile, newModelFile")
            print("  Optional: gridPath, polygon (defaults to 'null')")
            print("  Returns: data = 1 on success. newModelFile is written to disk.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_reformat(self):
        cstrings = self._common_strings()
        st5 = cstrings[-1]
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['newModelFile'])
        if self.config['gridOutputFile'] is None:
            st8 = "null"
        else:
            st8 = self.config['gridOutputFile']
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires configuration values: modelFile, newModelFile")
            print("  Optional: gridPath, gridOutputFile (defaults to 'null' which treats the output grid the same was as the input. Use '*' for internal to the newModelFile)")
            print("  Returns: data = 1 on success. newModelFile is written to disk.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_getValues(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        result = ResultError(stdout=None, stderr=None)
        if self.checkLatitude(self.config['pointLatitude']) is False:
            print("Error, pointLatitude must be between -90 and 90.")
            data = -2
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLatitude: {}".format(self.config['function'], self.config['pointLatitude']))
            return [result, data]
        if self.checkLongitude(self.config['pointLongitude']) is False:
            print("Error, pointLongitude must be between -180 and 360.")
            data = -3
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLongitude: {}".format(self.config['function'], self.config['pointLongitude']))
            return [result, data]
        st7 = str(self.config['pointLatitude'])
        st8 = str(self.config['pointLongitude'])
        st9 = str(self.config['pointDepth'])
        st10 = str(self.config['layerID'])
        self.setAttributeIndices()
        st11 = str(self.config['attributeIndices'])
        self.checkHorizontalInterpolation()
        st12 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st13 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True:
            self.config['reciprocal'] = False
        st14 = str(self.config['reciprocal'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires configuration values: modelFile, pointLatitude, pointLongitude, pointDepth, layerID, attributeIndices")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  Returns: data = ndarray of values")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_getValuesFile(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['inputPointsFile'])
        self.setAttributeIndices()
        st8 = str(self.config['attributeIndices'])
        self.checkHorizontalInterpolation()
        st9 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st10 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True:
            self.config['reciprocal'] = False
        st11 = str(self.config['reciprocal'])
        arglist = [st5, st6, st7, st8, st9, st10, st11]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires configuration values: modelFile, inputPointsFile, attributeIndices")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  Returns: data = npoints x nattributes NDArray")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_borehole(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        result = ResultError(stdout=None, stderr=None)
        if self.checkLatitude(self.config['pointLatitude']) is False:
            print("Error, pointLatitude must be between -90 and 90.")
            data = -2
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLatitude: {}".format(self.config['function'], self.config['pointLatitude']))
            return [result, data]
        if self.checkLongitude(self.config['pointLongitude']) is False:
            print("Error, pointLongitude must be between -180 and 360.")
            data = -3
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLongitude: {}".format(self.config['function'], self.config['pointLongitude']))
            return [result, data]
        st7 = str(self.config['pointLatitude'])
        st8 = str(self.config['pointLongitude'])
        st9 = str(self.config['maximumRadialSpacing'])
        st10 = str(self.config['deepestLayerID'])
        st11 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st12 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st13 = str(self.config['radialInterpolation'])
        self.checkVerticalOutputMode()
        st14 = str(self.config['verticalOutputMode'])
        if self.config['reciprocal'] != True:
            self.config['reciprocal'] = False
        st15 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st16 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, pointLatitude, pointLongitude, maximumRadialSpacing, deepestLayerID, shallowestLayerID, attributeIndices")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), verticalOutputMode (defaults to radius), reciprocal")
            print("  Returns: data = ndarray of values")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    
    def _execute_profile(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        result = ResultError(stdout=None, stderr=None)
        if self.checkLatitude(self.config['pointLatitude']) is False:
            print("Error, pointLatitude must be between -90 and 90.")
            data = -2
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLatitude: {}".format(self.config['function'], self.config['pointLatitude']))
            return [result, data]
        if self.checkLongitude(self.config['pointLongitude']) is False:
            print("Error, pointLongitude must be between -180 and 360.")
            data = -3
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLongitude: {}".format(self.config['function'], self.config['pointLongitude']))
            return [result, data]
        st7 = str(self.config['pointLatitude'])
        st8 = str(self.config['pointLongitude'])
        st9 = str(self.config['deepestLayerID'])
        st10 = str(self.config['shallowestLayerID'])
        self.checkVerticalOutputMode()
        st11 = str(self.config['verticalOutputMode'])
        if self.config['reciprocal'] != True:
            self.config['reciprocal'] = False
        st12 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st13 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, pointLatitude, pointLongitude, deepestLayerID, shallowestLayerID, attributeIndices")
            print("  Optional: gridPath, verticalOutputMode (defaults to radius), reciprocal")
            print("  Returns: data = ndarray of values")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_findClosestPoint(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        result = ResultError(stdout=None, stderr=None)
        if self.checkLatitude(self.config['pointLatitude']) is False:
            print("Error, pointLatitude must be between -90 and 90.")
            data = -2
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLatitude: {}".format(self.config['function'], self.config['pointLatitude']))
            return [result, data]
        if self.checkLongitude(self.config['pointLongitude']) is False:
            print("Error, pointLongitude must be between -180 and 360.")
            data = -3
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input pointLongitude: {}".format(self.config['function'], self.config['pointLongitude']))
            return [result, data]
        st7 = str(self.config['pointLatitude'])
        st8 = str(self.config['pointLongitude'])
        st9 = str(self.config['pointDepth'])
        st10 = str(self.config['layerID'])
        self.checkOutputChoices()
        st11 = str(self.config['outputChoices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, pointLatitude, pointLongitude, pointDepth, layerID")
            print("  Optional: gridPath")
            print("  Returns: data = ndarray of values defined in outputChoices")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result.setStdout('Run failed. See stderr.')
                result.setStderr("{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_slice(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['beginLongitude'])
        st9 = str(self.config['endLatitude'])
        st10 = str(self.config['endLongitude'])
        if self.config['shortestPath'] != False: # default to True
            self.config['shortestPath'] = True
        st11 = str(self.config['shortestPath'])
        if self.config['nPointsSlice'] < 0:
            print("Error, nPointsSlice must be positive.")
            data = -4
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input nPointsSlice: {}".format(self.config['function'], self.config['nPointsSlice']))
            return [result, data]
        st12 = str(int(self.config['nPointsSlice']))
        if self.config['maximumRadialSpacing'] < 0:
            print("Error, maximumRadialSpacing must be positive.")
            data = -5
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input maximumRadialSpacing: {}".format(self.config['function'], self.config['maximumRadialSpacing']))
            return [result, data]
        st13 = str(self.config['maximumRadialSpacing'])
        st14 = str(self.config['deepestLayerID'])
        st15 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st16 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st17 = str(self.config['radialInterpolation'])
        self.checkOutputSpatialCoords()
        st18 = str(self.config['outputSpatialCoords'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st19 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st20 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18, st19, st20]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, endLatitude, endLongitude, nPointsSlice, maximumRadialSpacing, deepestLayerID, shallowestLayerID, outputSpatialCoords, attributeIndices")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal, shortestPath (defaults to True)")
            print("  Returns: data = ndarray of values along the slice")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result.setStdout('Run failed. See stderr.')
                result.setStderr("{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_sliceDistAz(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        result = ResultError(stdout=None, stderr=None)
        if self.checkLatitude(self.config['beginLatitude']) is False:
            print("Error, beginLatitude must be between -90 and 90.")
            data = -2
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input beginLatitude: {}".format(self.config['function'], self.config['beginLatitude']))
            return [result, data]
        if self.checkLongitude(self.config['beginLongitude']) is False:
            print("Error, beginLongitude must be between -180 and 360.")
            data = -3
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input beginLongitude: {}".format(self.config['function'], self.config['beginLongitude']))
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['beginLongitude'])
        st9 = str(self.config['degDistanceToLastPoint'])
        st10 = str(self.config['azimuthToLastPoint'])
        if self.config['nPointsSlice'] < 0:
            print("Error, nPointsSlice must be positive.")
            data = -4
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input nPointsSlice: {}".format(self.config['function'], self.config['nPointsSlice']))
            return [result, data]
        st12 = str(int(self.config['nPointsSlice']))
        if self.config['maximumRadialSpacing'] < 0:
            print("Error, maximumRadialSpacing must be positive.")
            data = -5
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input maximumRadialSpacing: {}".format(self.config['function'], self.config['maximumRadialSpacing']))
            return [result, data]
        st11 = str(self.config['nPointsSlice'])
        st12 = str(self.config['maximumRadialSpacing'])
        st13 = str(self.config['deepestLayerID'])
        st14 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st15 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st16 = str(self.config['radialInterpolation'])
        self.checkOutputSpatialCoords()
        st17 = str(self.config['outputSpatialCoords'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st18 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st19 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18, st19]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, degDistanceToLastPoint, azimuthToLastPoint, nPointsSlice, maximumRadialSpacing, deepestLayerID, shallowestLayerID, outputSpatialCoords, attributeIndices")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  Returns: data = ndarray of values along the slice")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result.setStdout('Run failed. See stderr.')
                result.setStderr("{} function error.".format(self.config['function']))
        return [result, data]

    def _execute_mapValues(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['endLatitude'])
        # Switch for integer number or spacing
        if self.config['mapSpatialSamplingMode'] == 'number':
            st9 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st9 = str(float(self.config['spacingLatitudeMap']))
        else:
            st9 = str(float(self.config['spacingLatitudeMap']))
        st10 = str(self.config['beginLongitude'])
        st11 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st12 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st12 = str(float(self.config['spacingLongitudeMap']))
        else:
            st12 = str(float(self.config['spacingLongitudeMap']))
        st13 = str(self.config['layerID'])
        if self.config['function'] == 'mapValuesDepth':
            st14 = str(self.config['pointDepth'])
        else:
            st14 = str(self.config['fractionalRadius'])
        self.checkHorizontalInterpolation()
        st15 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st16 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st17 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st18 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, endLatitude, endLongitude, layerID, attributeIndices, pointDepth (mapValuesDepth) or fractionalRadius (mapValuesLayer), mapSpatialSamplingMode (number or spacing)")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  If mapSpatialSamplingMode == 'number', then nLongitudeMap and nLatitudeMap should be integers.")
            print("  If mapSpatialSamplingMode == 'spacing', then spacingLongitudeMap and spacingLatitudeMap should be doubles.")
            print("  Returns: data = ndarray of values for the map")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result.setStdout('Run failed. See stderr.')
                result.setStderr("{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_mapLayerBoundary(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['endLatitude'])
        # Switch for integer number or spacing
        if self.config['mapSpatialSamplingMode'] == 'number':
            st9 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st9 = str(float(self.config['spacingLatitudeMap']))
        else:
            st9 = str(float(self.config['spacingLatitudeMap']))
        st10 = str(self.config['beginLongitude'])
        st11 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st12 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st12 = str(float(self.config['spacingLongitudeMap']))
        else:
            st12 = str(float(self.config['spacingLongitudeMap']))
        st13 = str(self.config['layerID'])
        st14 = str(self.config['topOrBottomOfLayer'])
        st15 = str(self.config['verticalOutputMode'])
        self.checkHorizontalInterpolation()
        st16 = str(self.config['horizontalInterpolation'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, endLatitude, endLongitude, layerID, mapSpatialSamplingMode, topOrBottomOfLayer, verticalOutputMode")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear)")
            print("  If mapSpatialSamplingMode == 'number', then nLongitudeMap and nLatitudeMap should be integers.")
            print("  If mapSpatialSamplingMode == 'spacing', then spacingLongitudeMap and spacingLatitudeMap should be doubles.")
            print("  Returns: data = ndarray of values for the map")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_mapLayerThickness(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['endLatitude'])
        # Switch for integer number or spacing
        if self.config['mapSpatialSamplingMode'] == 'number':
            st9 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st9 = str(float(self.config['spacingLatitudeMap']))
        else:
            st9 = str(float(self.config['spacingLatitudeMap']))
        st10 = str(self.config['beginLongitude'])
        st11 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st12 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st12 = str(float(self.config['spacingLongitudeMap']))
        else:
            st12 = str(float(self.config['spacingLongitudeMap']))
        st13 = str(self.config['deepestLayerID'])
        st14 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st15 = str(self.config['horizontalInterpolation'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, endLatitude, endLongitude, mapSpatialSamplingMode, deepestLayerID, shallowestLayerID")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  If mapSpatialSamplingMode == 'number', then nLongitudeMap and nLatitudeMap should be integers.")
            print("  If mapSpatialSamplingMode == 'spacing', then spacingLongitudeMap and spacingLatitudeMap should be doubles.")
            print("  Returns: data = ndarray of values for the map")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_values3DBlock(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st7 = str(self.config['beginLatitude'])
        st8 = str(self.config['endLatitude'])
        # Switch for integer number or spacing
        if self.config['mapSpatialSamplingMode'] == 'number':
            st9 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st9 = str(float(self.config['spacingLatitudeMap']))
        else:
            st9 = str(float(self.config['spacingLatitudeMap']))
        st10 = str(self.config['beginLongitude'])
        st11 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st12 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st12 = str(float(self.config['spacingLongitudeMap']))
        else:
            st12 = str(float(self.config['spacingLongitudeMap']))
        st13 = str(self.config['deepestLayerID'])
        st14 = str(self.config['shallowestLayerID'])
        st15 = str(self.config['verticalOutputMode'])
        st16 = str(self.config['maximumRadialSpacing'])
        self.checkHorizontalInterpolation()
        st17 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st18 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st19 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st20 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18, st19, st20]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, beginLatitude, beginLongitude, endLatitude, endLongitude, mapSpatialSamplingMode, attributeIndices, deepestLayerID, shallowestLayerID, verticalOutputMode, maximumRadialSpacing")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  If mapSpatialSamplingMode == 'number', then nLongitudeMap and nLatitudeMap should be integers.")
            print("  If mapSpatialSamplingMode == 'spacing', then spacingLongitudeMap and spacingLatitudeMap should be doubles.")
            print("  Returns: data = ndarray of values for the block")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_function(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['attributeIndex0'])
        st8 = self.config['secondModel']
        if self.config['secondGridPath'] is None:
            st9 = '.'
        else:
            st9 = self.config['secondGridPath']
        st10 = str(self.config['attributeIndex1'])
        st11 = self.config['geometryModel']
        if self.config['geometryModelPath'] is None:
            st12 = '.'
        else:
            st12 = self.config['geometryModelPath']
        st13 = self.config['newModelFile']
        if self.config['gridOutputFile'] is not None:
            st14 = self.config['gridOutputFile']
        else:
            st14 = "null"
        if self.config['functionIndex'] >= 0 and self.config['functionIndex'] <= 4:
            st15 = str(self.config['functionIndex'])
        else:
            print("Error, unable to run GeoTessExplorer.")
            data = -5
            result = ResultError(stdout="Inappropriate function value.", stderr="function must be between 0 and 4. 'functionIndex' value entered: {}".format(self.config['functionIndex']))
            return [result, data]
        st16 = str(self.config['newAttributeName'])
        st17 = str(self.config['newAttributeUnits'])
        self.checkHorizontalInterpolation()
        st18 = str(self.config['horizontalInterpolation'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, attributeIndex0, secondModel, attributeIndex1, geometryModel, newModelFile, functionIndex (integer between 0 and 4 inclusive), newAttributeName, newAttributeUnits")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), secondGridPath, geometryModelPath, gridOutputFile")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkLayers(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = self.check_file_stub_string()
        self.check_layer_indices()
        st8 = str(self.config['layerIndices'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st9 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st10 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, outputFileNameStub, layerIndices, attributeIndices")
            print("  Optional: gridPath, reciprocal")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkDepths(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st8 = str(self.config['layerID'])
        st9 = str(self.config['firstDepth'])
        st10 = str(self.config['lastDepth'])
        st11 = str(self.config['depthSpacing'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st12 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st13 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, layerID, firstDepth, lastDepth, depthSpacing")
            print("  Optional: gridPath, reciprocal")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkDepths2(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st8 = str(self.config['layerID'])
        self.check_depth_list()
        st9 = str(self.config['depths'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st10 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st11 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, layerID, depths")
            print("  Optional: gridPath, reciprocal")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
                  
    def _execute_vtkLayerThickness(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st8 = str(self.config['deepestLayerID'])
        st9 = str(self.config['shallowestLayerID'])
        self.checkRadialInterpolation()
        st10 = str(self.config['radialInterpolation'])
        arglist = [st5, st6, st7, st8, st9, st10]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, deepestLayerID, shallowestLayerID")
            print("  Optional: gridPath, reciprocal, radialInterpolation")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkLayerBoundary(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        self.check_depthOrElevation()
        st8 = str(self.config['depthOrElevation'])
        self.checkRadialInterpolation()
        st9 = str(self.config['radialInterpolation'])
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName")
            print("  Optional: gridPath, radialInterpolation, depthOrElevation")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkSlice(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st8 = str(self.config['beginLatitude'])
        st9 = str(self.config['beginLongitude'])
        st10 = str(self.config['endLatitude'])
        st11 = str(self.config['endLongitude'])
        if self.config['shortestPath'] != False: # default to True
            self.config['shortestPath'] = True
        st12 = str(self.config['shortestPath'])
        if self.config['nPointsSlice'] < 0:
            print("Error, nPointsSlice must be positive.")
            data = -4
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input nPointsSlice: {}".format(self.config['function'], self.config['nPointsSlice']))
            return [result, data]
        st13 = str(int(self.config['nPointsSlice']))
        if self.config['maximumRadialSpacing'] < 0:
            print("Error, maximumRadialSpacing must be positive.")
            data = -5
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input maximumRadialSpacing: {}".format(self.config['function'], self.config['maximumRadialSpacing']))
            return [result, data]
        st14 = str(self.config['maximumRadialSpacing'])
        st15 = str(self.config['deepestLayerID'])
        st16 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st17 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st18 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st19 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st20 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18, st19, st20]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, beginLatitude, beginLongitude, endLatitude, endLongitude, nPointsSlice, maximumRadialSpacing, deepestLayerID, shallowestLayerID, attributeIndices")
            print("  Optional: gridPath, shortestPath, horizontalInterpolation, radialInterpolation, reciprocal")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkSolid(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        if self.config['maximumRadialSpacing'] < 0:
            print("Error, maximumRadialSpacing must be positive.")
            data = -5
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input maximumRadialSpacing: {}".format(self.config['function'], self.config['maximumRadialSpacing']))
            return [result, data]
        st8 = str(self.config['maximumRadialSpacing'])
        st9 = str(self.config['deepestLayerID'])
        st10 = str(self.config['shallowestLayerID'])
        self.checkHorizontalInterpolation()
        st11 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st12 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st13 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st14 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, maximumRadialSpacing, deepestLayerID, shallowestLayerID, attributeIndices")
            print("  Optional: gridPath, shortestPath, horizontalInterpolation, radialInterpolation, reciprocal")
            print("  Returns: data = 1 on successfully written newModelFile to disk")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtk3DBlock(self):
        cstrings = self._common_strings()
        st5 = cstrings[-1]
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st8 = str(self.config['beginLatitude'])
        st9 = str(self.config['endLatitude'])
        # Switch for integer number or spacing
        if self.config['mapSpatialSamplingMode'] == 'number':
            st10 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st10 = str(float(self.config['spacingLatitudeMap']))
        else:
            st10 = str(float(self.config['spacingLatitudeMap']))
        st11 = str(self.config['beginLongitude'])
        st12 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st13 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st13 = str(float(self.config['spacingLongitudeMap']))
        else:
            st13 = str(float(self.config['spacingLongitudeMap']))
        st14 = str(self.config['deepestLayerID'])
        st15 = str(self.config['shallowestLayerID'])
        st16 = str(self.config['verticalOutputMode'])
        st17 = str(self.config['maximumRadialSpacing'])
        self.checkHorizontalInterpolation()
        st18 = str(self.config['horizontalInterpolation'])
        self.checkRadialInterpolation()
        st19 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st20 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st21 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14, st15, st16, st17, st18, st19, st20, st21]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, beginLatitude, beginLongitude, endLatitude, endLongitude, mapSpatialSamplingMode, attributeIndices, deepestLayerID, shallowestLayerID, verticalOutputMode, maximumRadialSpacing")
            print("  Optional: gridPath, horizontalInterpolation (defaults to linear), radialInterpolation (defaults to linear), reciprocal")
            print("  If mapSpatialSamplingMode == 'number', then nLongitudeMap and nLatitudeMap should be integers.")
            print("  If mapSpatialSamplingMode == 'spacing', then spacingLongitudeMap and spacingLatitudeMap should be doubles.")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkPoints(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        st5 = str(self.config['inputPointsFile'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st6 = str(self.config['vtkOutputFileName'])
        arglist = [st5, st6]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: inputPointsFile, vtkOutputFileName")
            print("  Optional: None")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkRobinson(self):
        cstrings = self._common_strings()
        st5 = cstrings[-1]
        nstr = len(cstrings)
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['vtkOutputFileName'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        if self.checkLongitude(self.config['centerLongitude']) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function error. centerLongitude must be between -180 and 360 ".format(self.config['function']))
            return [result, data]
        st8 = str(self.config['centerLongitude'])
        st9 = str(self.config['pointDepth'])
        st10 = str(self.config['layerID'])
        if self.config['radiusOutOfRangeAllowed'] != True:
            self.config['radiusOutOfRangeAllowed'] = False
        st11 = str(self.config['radiusOutOfRangeAllowed'])
        self.checkRadialInterpolation()
        st12 = str(self.config['radialInterpolation'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st13 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st14 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11, st12, st13, st14]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, centerLongitude, pointDepth, attributeIndices, layerID")
            print("  Optional: gridPath, radialInterpolation (defaults to linear), reciprocal, radiusOutOfRangeAllowed (defaults to False)")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkRobinsonLayers(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = self.check_file_stub_string_int()
        if self.checkLongitude(self.config['centerLongitude']) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function error. centerLongitude must be between -180 and 360 ".format(self.config['function']))
            return [result, data]
        st8 = str(self.config['centerLongitude'])
        self.check_layer_indices()
        st9 = str(self.config['layerIndices'])
        if self.config['reciprocal'] != True: # default to False
            self.config['reciprocal'] = False
        st10 = str(self.config['reciprocal'])
        self.setAttributeIndices()
        st11 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7, st8, st9, st10, st11]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, outputFileNameStub, centerLongitude, attributeIndices, layerIndices")
            print("  Optional: gridPath, reciprocal")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                #print([st1, st2, st3, st4, st5, st6, st7, st8, st9])
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkRobinsonPoints(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        st5 = str(self.config['inputPointsFile'])
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st6 = str(self.config['vtkOutputFileName'])
        if self.checkLongitude(self.config['centerLongitude']) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function error. centerLongitude must be between -180 and 360 ".format(self.config['function']))
            return [result, data]
        st7 = str(self.config['centerLongitude'])
        arglist = [st5, st6, st7]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: inputPointsFile, vtkOutputFileName, centerLongitude")
            print("  Optional: None")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkRobinsonTriangleSize(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st7 = str(self.config['vtkOutputFileName'])
        st8 = str(self.config['layerID'])
        if self.checkLongitude(self.config['centerLongitude']) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function error. centerLongitude must be between -180 and 360 ".format(self.config['function']))
            return [result, data]
        st9 = str(self.config['centerLongitude'])
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile (can be a grid), vtkOutputFileName, layerID, centerLongitude")
            print("  Optional: gridPath")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_vtkLayerAverage(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.check_vtk_extension(str(self.config['vtkOutputFileName'])) is False:
            data = -2
            result = ResultError(stdout="Run failed. See stderr", stderr="{} function requires vtkOutputFileName end in '.vtk'.".format(self.config['function']))
            return [result, data]
        st6 = str(self.config['vtkOutputFileName'])
        st7 = str(self.config['layerID'])
        self.setAttributeIndices()
        st8 = str(self.config['attributeIndices'])
        arglist = [st5, st6, st7]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.config['attributeIndices'] is not None:
            cstrings.append(st8)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires: modelFile, vtkOutputFileName, layerID")
            print("  Optional: attributeIndices")
            print("  Returns: 1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_getLatitudes(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        if self.checkLatitude(self.config['beginLatitude']) is False:
            data = -91
            result = ResultError(stdout="beginLatitude malformed.", stderr = "{} function got malformed beginLatitude: {}.".format(self.config['function'], self.config['beginLatitude']))
            return [result, data]
        if self.checkLatitude(self.config['endLatitude']) is False:
            data = -92
            result = ResultError(stdout="endLatitude malformed.", stderr = "{} function got malformed endLatitude: {}.".format(self.config['function'], self.config['endLatitude']))
            return [result, data]
        st5 = str(self.config['beginLatitude'])
        st6 = str(self.config['endLatitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st7 = str(int(self.config['nLatitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st7 = str(float(self.config['spacingLatitudeMap']))
        else:
            st7 = str(float(self.config['spacingLatitudeMap']))
        arglist = [st5, st6, st7]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires:  beginLatitude, beginLatitude, mapSpatialSamplingMode, [nLatitudeMap or spacingLatitudeMap]")
            print("  Optional: None")
            print("  Note: if mapSpatialSamplingMode is 'number', give nLatitudeMap for number of latitude values to return.")
            print("        if mapSpatialSamplingMode is 'spacing', give spacingLatitudeMap for step size in latitude, aka dy.")
            print("        Defaults to spacing mode.")
            print("  Returns: array of latitudes on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]

    def _execute_getLongitudes(self):
        cstrings = self._common_strings()
        nst = len(cstrings)
        cstrings.pop()
        if self.checkLongitude(self.config['beginLongitude']) is False:
            data = -181
            result = ResultError(stdout="beginLongitude malformed.", stderr = "{} function got malformed beginLongitude: {}.".format(self.config['function'], self.config['beginLongitude']))
            return [result, data]
        if self.checkLongitude(self.config['endLongitude']) is False:
            data = -182
            result = ResultError(stdout="endLongitude malformed.", stderr = "{} function got malformed endLongitude: {}.".format(self.config['function'], self.config['endLongitude']))
            return [result, data]
        st5 = str(self.config['beginLongitude'])
        st6 = str(self.config['endLongitude'])
        if self.config['mapSpatialSamplingMode'] == 'number':
            st7 = str(int(self.config['nLongitudeMap']))
        elif self.config['mapSpatialSamplingMode'] == 'spacing':
            st7 = str(float(self.config['spacingLongitudeMap']))
        else:
            st7 = str(float(self.config['spacingLongitudeMap']))
        arglist = [st5, st6, st7]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires:  beginLatitude, beginLatitude, mapSpatialSamplingMode, [nLongitudeMap or spacingLongitudeMap]")
            print("  Optional:  None")
            print("  Note: if mapSpatialSamplingMode is 'number', give nLongitudeMap for number of longitude values to return.")
            print("        if mapSpatialSamplingMode is 'spacing', give spacingLongitudeMap for step size in longitude, aka dx.")
            print("        Defaults to spacing mode.")
            print("  Returns: array of longitudes on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_getDistanceDegrees(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        [result, data, no_error] = self.check_map_bounds()
        if no_error is False:
            return [result, data]
        st5 = str(self.config['beginLatitude'])
        st6 = str(self.config['beginLongitude'])
        st7 = str(self.config['endLatitude'])
        st8 = str(self.config['endLongitude'])
        if self.config['nPointsSlice'] < 0:
            print("Error, nPointsSlice must be positive.")
            data = -4
            result.setStdout('Run failed. See stderr.')
            result.setStderr("{} function error. Input nPointsSlice: {}".format(self.config['function'], self.config['nPointsSlice']))
            return [result, data]
        st9 = str(int(self.config['nPointsSlice']))
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires:  beginLatitude, beginLatitude, endLatitude, endLongitude, nPointsSlice")
            print("  Optional:  None")
            print("  Returns: NDArray of distances in degrees along the great circle from begin to end.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_translatePolygon(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        cstrings.pop()
        ext = self.config['inputPolygon'][-4:]
        if ext not in ['.kml', '.kmz']:
            print("Error, inputPolygon must end in .kml or .kmz")
            result = ResultError(stdout="translatePolygon given bad inputPolygon file name. Must end in .kml or .kmz", stderr="bad inputPolygon: {}. Must end in .kml or .kmz. ".format(self.config['inputPolygon']))
            data = -4
            return [result, data]
        st5 = str(self.config['inputPolygon'])
        st6 = str(self.config['outputPolygon'])
        arglist = [st5, st6]
        for arg in arglist:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("Function: {}".format(self.config['function']))
            print("  Requires:  inputPolygon, outputPolygon")
            print("  Optional:  None")
            print("  Returns:   1 on success.")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                data = -1
                result = ResultError(stdout="Run failed. See stderr", stderr="{} function error.".format(self.config['function']))
        return [result, data]
    
    def _execute_extractSiteTerms(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        arglist = [st5, st6]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("  Requires: modelFile")
            print("  Optional: gridPath")
            print("  Returns: DataFrame containing station, lat, lon, elev, ondate, offdate, site terms")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = self.getOutput(result)
                #data = self.getSiteTerms(result.stdout, mode='string')
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="ExtractSiteTerms Failed. Make sure GeoTessModel ClassName is GeoTessModelSiteData.")
                data = -1
        return [result, data]
    
    def _execute_replaceSiteTerms(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['newSiteTermFile'])
        st8 = str(self.config['newModelFile'])
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("  Requires: modelFile, newSiteTermFile, newModelFile")
            print("  Optional: gridPath")
            print("  Returns:  1 on success")
            print(" ")
            print("  The newSiteTermFile can be written from a DataFrame with 'writeSiteTermsDFtoFile'")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
                #data = self.getSiteTerms(result.stdout, mode='string')
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="replaceSiteTerms Failed. Make sure GeoTessModel ClassName is GeoTessModelSiteData.")
                data = -1
        return [result, data]
    
    def _execute_extractPathDependentUncertaintyRSTT(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        if self.checkRSTT() is False:
            print("Error, unable to run GeoTessExplorer.")
            result = ResultError(stdout="Run failed. See stderr", stderr="Check RSTT_FileNameStub: {} contains <phase> (braces and outside triple quote required) and RSTT_OutputType is one of 'binary', 'ascii', or 'geotess'. Given: {}".format(str(self.config['RSTT_FileNameStub']), str(self.config['RSTT_OutputType'])))
            data = -2
            return [result, data]
        st7 = self.config['RSTT_FileNameStub']
        st8 = str(self.config['RSTT_OutputType'])
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("  Requires: modelFile, RSTT_FileNameStub, RSTT_OutputType")
            print("  Optional: gridPath")
            print("  Returns:  1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                for line in result.stdout.split("\n"):
                    strings = line.split(" ")
                    if strings[0] == "Writing":
                        fname = strings[2]
                        fnam = fname.replace("'", "")
                        os.rename(fname, fnam)
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="extractPathDependentUncertaintyRSTT Failed. Make sure GeoTessModel ClassName is GeoTessModelSLBM.")
                data = -1
        return [result, data]
    
    def _execute_replacePathDependentUncertaintyRSTT(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        self.config['RSTT_OutputType'] = 'geotess'
        if self.checkRSTT() is False:
            print("Error, unable to run GeoTessExplorer.")
            result = ResultError(stdout="Run failed. See stderr", stderr="Check RSTT_FileNameStub: {} contains <phase> (braces and triple quote included)".format(str(self.config['RSTT_FileNameStub'])))
            data = -2
            return [result, data]
        st7 = self.config['RSTT_FileNameStub']
        st8 = str(self.config['newModelFile'])
        arglist = [st5, st6, st7, st8]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("  Requires: modelFile, RSTT_FileNameStub, newModelFile")
            print("  Optional: gridPath")
            print("  Returns:  1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="extractPathDependentUncertaintyRSTT Failed. Make sure GeoTessModel ClassName is GeoTessModelSLBM.")
                data = -1
        return [result, data]
    
    def _execute_renameLayer(self):
        cstrings = self._common_strings()
        nstr = len(cstrings)
        st5 = cstrings[-1]
        if self.config['gridPath'] is None:
            st6 = '.'
        else:
            st6 = self.config['gridPath']
        st7 = str(self.config['newModelFile'])
        st8 = str(self.config['oldLayerName'])
        st9 = str(self.config['newLayerName'])
        arglist = [st5, st6, st7, st8, st9]
        for arg in arglist[1:]:
            cstrings.append(arg)
        if self.check_nones(arglist):
            print("  Requires: modelFile, newModelFile, oldLayerName, newLayerName")
            print("  Optional: None")
            print("  Returns:  1 on success")
            result = subprocess.run(cstrings[:nstr-1], check=True, text=True, capture_output=self.config['captureOutput'])
            data = 0
        else:
            try:
                result = subprocess.run(cstrings, check=True, text=True, capture_output=self.config['captureOutput'])
                data = 1
            except:
                print("Error, unable to run GeoTessExplorer.")
                result = ResultError(stdout="Run failed. See stderr", stderr="{}. Function failed. Params: modelFile: {}. newModelFile: {}. oldLayerName: {}. newLayerName: {}".format(self.config['function'], self.config['modelFile'], self.config['newModelFile'], self.config['oldLayerName'], self.config['newLayerName']))
                data = -1
        return [result, data]
            
    
    def checkRSTT(self):
        if self.config['RSTT_OutputType'] not in ['ascii', 'binary', 'geotess']:
            return False
        if "'<phase>'" in self.config['RSTT_FileNameStub']:
            return True
        elif "<phase>" in self.config['RSTT_FileNameStub']:
            return True
        else:
            return False
        return True
        
                
    def check_file_stub_string(self):
        if "%d" or "%s" in self.config['outputFileNameStub']:
            return str(self.config['outputFileNameStub'])
        else:
            return None
        return
    
    def check_file_stub_string_int(self):
        if "%d" in self.config['outputFileNameStub']:
            return str(self.config['outputFileNameStub'])
        else:
            return None
        return
    
    def check_depth_list(self):
        if isinstance(self.config['depths'], str):
            self.config['depths'] = self.config['depths'].replace(" ", "")
        elif isinstance(self.config['depths'], list): 
            tmp = ""
            for val in self.config['depths']:
                if val != self.config['depths'][-1]:
                    tmp += str(val) + ","
                else:
                    tmp += str(val)
            self.config['depths'] = tmp
        else:
            self.config['depths'] = None
        return
                  
    def check_layer_indices(self):
        if isinstance(self.config['layerIndices'], str):
            self.config['layerIndices'] = self.config['layerIndices'].replace(" ", "")
        elif isinstance(self.config['layerIndices'], list): 
            tmp = ""
            for val in self.config['layerIndices']:
                if val != self.config['layerIndices'][-1]:
                    tmp += str(val) + ","
                else:
                    tmp += str(val)
            self.config['layerIndices'] = tmp
        else:
            self.config['layerIndices'] = None
        return
        
    def check_map_bounds(self):
        result = ResultError(stdout=None, stderr=None)
        data = 0
        no_errors = True
        if self.checkLatitude(self.config['beginLatitude']) is False:
            print("Error, latitude must be between -90 and 90.")
            data += -2
            result.appendStdout('Run failed. See stderr.')
            result.appendStderr("{} function error. Input beginLatitude: {}".format(self.config['function'], self.config['beginLatitude']))
            no_errors = False
        if self.checkLongitude(self.config['beginLongitude']) is False:
            print("Error, longitude must be between -180 and 360.")
            data += -3
            result.appendStdout('Run failed. See stderr.')
            result.appendStderr("{} function error. Input beginLongitude: {}".format(self.config['function'], self.config['beginLongitude']))
            no_errors = False
        if self.checkLatitude(self.config['endLatitude']) is False:
            print("Error, latitude must be between -90 and 90.")
            data += -2
            result.appendStdout('Run failed. See stderr.')
            result.appendStderr("{} function error. Input endLatitude: {}".format(self.config['function'], self.config['endLatitude']))
            no_errors = False
        if self.checkLongitude(self.config['endLongitude']) is False:
            print("Error, longitude must be between -180 and 360.")
            data += -3
            result.appendStdout('Run failed. See stderr.')
            result.appendStderr("{} function error. Input endLongitude: {}".format(self.config['function'], self.config['endLongitude']))
            no_errors = False
        return [result, data, no_errors]
    
    @staticmethod
    def check_vtk_extension(mystring):
        if mystring[-4:] != ".vtk":
            return False
        return True

    
    @staticmethod
    def check_nones(arglist):
        for arg in arglist:
            if arg is None or arg == 'None':
                return True
        return False
    
    def checkOutputSpatialCoords(self):
        if isinstance(self.config['outputSpatialCoords'], str):
            self.config['outputSpatialCoords'] = self.config['outputSpatialCoords'].replace(" ", "")
        else:
            tmp = ""
            for val in self.config['outputSpatialCoords']:
                if val in ['distance', 'depth', 'radius', 'x' 'y', 'z', 'lat', 'lon']:
                    if val != self.config['outputSpatialCoords'][-1]:
                        tmp += str(val) + ","
                    else:
                        tmp += str(val)
            self.config['outputSpatialCoords'] = tmp
        return
    
    def checkVerticalOutputMode(self):
        if self.config['verticalOutputMode'] is None:
            self.config['verticalOutputMode'] = 'radius'
        elif self.config['verticalOutputMode'] not in ['radius', 'depth']:
            self.config['verticalOutputMode'] = 'radius'
        return   
    
    def check_depthOrElevation(self):
        if self.config['depthOrElevation'] is None:
            self.config['depthOrElevation'] = 'depth'
        elif self.config['depthOrElevation'] not in ['elevation', 'depth']:
            self.config['depthOrElevation'] = 'depth'
        return   
     
    @staticmethod
    def getBoreholeResults(result):
        lines = result.stdout.split("\n")
        data = GeoTessExplorer.read_ascii_data_string(lines)
        return data[:-1]
    
    @staticmethod
    def getInterpolatePoint(result):
        return "This is a verbose version of getValues. Data is not returned as a Python variable." + "\n" + result.stdout
    
    @staticmethod
    def getValuesOutput(result):
        mystring = result.stdout
        info = mystring.split(" ")
        tmp = []
        for val in info:
            if val != "" and val != "\n":
                tmp.append(val)
        pts = np.zeros((len(tmp),))
        for ival, val in enumerate(tmp):
            pts[ival] = float(val)
        return pts
    
    @staticmethod
    def getValuesFileOutput(result):
        lines = result.stdout.split("\n")
        grd = GeoTessExplorer.read_ascii_data_string(lines)
        data = grd[:-1] # stdout includes an extra empty line that results in a bunch of 0s at the end
        return data
    
    def getExtractActiveNodesOutput(self, result):
        lines = result.stdout.split("\n")
        info = lines[0].split(" ")
        if len(info) == 2:
            data = self.read_gtgrid_2col(lines)
        else:
            data = self.read_ascii_data_string(lines)
        return data
    
    @staticmethod
    def read_ascii_data_string(lines):
        info = lines[0].split(" ")
        while ("") in info:
            info.remove("")
        griddata = np.zeros((len(lines), len(info)))
        for iline, line in enumerate(lines):
            info = line.split(" ")
            while ("") in info:
                info.remove("")
            for idx, ival in enumerate(info):
                #print(iline, idx, ival, len(line), len(info))
                griddata[iline, idx] = ival
        return griddata
        
    
    # Made assuming only 1 attribute.
    # Superceded for more flexible version
    @staticmethod
    def read_3D_ascii_data_string(lines):
        griddata = np.zeros((len(lines), 5))
        for iline, line in enumerate(lines):
            info = line.split(" ")
            if len(info) == 5:
                lat = info[0]
                lon = info[1]
                depth = info[2]
                layer = info[3]
                attribute = info[4]
                griddata[iline, 0] = lat
                griddata[iline, 1] = lon
                griddata[iline, 2] = depth
                griddata[iline, 3] = layer
                griddata[iline, 4] = attribute
        return griddata
    
    # Made assuming only 1 attribute.
    # Superceded for more flexible version
    @staticmethod
    def read_2D_ascii_data_string(lines):
        griddata = np.zeros((len(lines),3))
        for iline, line in enumerate(lines):
            info = line.split(" ")
            if len(info) == 3:
                lat = info[0]
                lon = info[1]
                attribute = info[2]
                griddata[iline, 0] = lat
                griddata[iline, 1] = lon
                griddata[iline, 2] = attribute
        return griddata
    
    @staticmethod
    def read_gtgrid_2col(lines):
        griddata = np.zeros((len(lines), 2))
        for iline, line in enumerate(lines):
            info = line.split(" ")
            if len(info) == 2:
                lat = info[0]
                lon = info[1]
                griddata[iline, 0] = lat
                griddata[iline, 1] = lon
        return griddata

    @staticmethod
    def readGridStdout(grid_string):
        lines = grid_string.split("\n")
        griddata = np.zeros((len(lines), 4))
        for iline, line in enumerate(lines):
            info = line.split(" ")
            if len(info) == 4:
                lat1 = info[0]
                lon1 = info[1]
                lat2 = info[2]
                lon2 = info[3]
                griddata[iline, 0] = lat1
                griddata[iline, 1] = lon1
                griddata[iline, 2] = lat2
                griddata[iline, 3] = lon2
        return griddata
    
    @staticmethod
    def checkLatitude(lat):
        if lat is None:
            return False
        if lat < -90 or lat > 90:
            return False
        return True
    
    @staticmethod
    def checkLongitude(lon):
        if lon is None:
            return False
        if lon < -180 or lon > 360:
            return False
        return True
    


    @staticmethod
    def readGridGMT(grid_string, writeFile = False, outputFileName = 'GeoTessExtractGrid.gmt'):
        """
        Reads a GMT formatted extracted grid.
        Note that it outputs:
        lon lat
        lon lat
        >
        lon lat
        lon lat
        >
        to define triangle edges

        For GMT plotting use writeFile = True and outputFileName = something
        This routine reads into a numpy array in the same format as stdout
        ie:
            n x 4
            lat1 lon1 lat2 lon2
        where n is the number of connections
        """
        import numpy as np
        lines = grid_string.split("\n")

        # Prepares an output file if writeFile is set to True
        if writeFile:
            outFile = open(outputFileName, 'w')

        # First loop just counts the number of connections and writes the gmt format if requested
        n = 0
        for iline, line in enumerate(lines):
            if writeFile:
                outFile.write(line)
                outFile.write("\n")
            if len(line) > 0:
                if line[0] == ">":
                    n += 1

        # closes the output file
        if writeFile:
            outFile.close()

        # Allocate memory and run second read
        griddata = np.zeros((n, 4))
        ndx = -1
        for iline, line in enumerate(lines):
            info = line.split(" ")
            if len(line) > 0:
                if info[0] == ">":
                    ndx += 1
                    jdx = 0
                if len(info) == 2:
                    griddata[ndx, jdx] = info[1]
                    griddata[ndx, jdx+1] = info[0]
                    jdx = 2
        return griddata
    
    @staticmethod
    def readStatisticsTable(stat_string):
        """
        Reads a table in the format of a string and parses into a pandas dataFrame object
        """
        lines = stat_string.split("\n")
        for iline, line in enumerate(lines):
            if iline == 0:
                lf = line.split("\t")
                lff = lf[0].split(" ")
                while ("" in lff):
                    lff.remove("")
                column_names = lff.copy()
                df = pd.DataFrame(columns = column_names)
            else:
                lf = line.split("\t")
                lff = lf[0].split(" ")
                while ("" in lff):
                    lff.remove("")
                if len(lff) > 0:
                    layerID = lff[0]
                    attribute = lff[1] + " " + lff[2]
                    nVertex = lff[3]
                    nNodes = lff[4]
                    nNaN = lff[5]
                    nValid = lff[6]
                    Min = lff[7]
                    Max = lff[8]
                    Mean = lff[9]
                    Median = lff[10]
                    StdDev = lff[11]
                    MAD = lff[12]
                    d = {'Layer': layerID, 'Attribute': attribute, 'nVertex': nVertex, 'nNodes': nNodes,
                        'nNaN': nNaN, 'nValid': nValid, 'Min': Min, 'Max': Max, 'Mean': Mean, 'Median': Median,
                        'StdDev': StdDev, 'MAD': MAD}
                    s1 = pd.DataFrame(d, index=[iline-1])
                    df = pd.concat([df, s1])
                    df.reset_index()
        return df
    
    @staticmethod
    def getSiteTerms(site_terms, mode='string'):
        """
        Reads a string of output from the GeoTessExplorer function "extractSiteTerms"
        Returns output as a Pandas DataFrame
        """
        count = 0
        nstation = 0
        stations = []
        if mode == 'file':
            with open(site_terms) as fp:
                Lines = fp.readlines()
                nlines = len(Lines)-3
        else:
            Lines = site_terms.split("\n")
            nlines = len(Lines)-4
        latitudes = np.zeros((nlines,))
        longitudes = np.zeros((nlines,))
        elevations = np.zeros((nlines,))
        ondates = np.zeros((nlines,))
        offdates = np.zeros((nlines,))
        for iline, line in enumerate(Lines):
            count += 1
            strings = line.split(" ")
            if count == 1:
                labels = strings[1:]
                cleanLabels = []
                for label in labels:
                    cleanLabels.append(label.strip(";").strip('\n'))
            elif count == 2:
                units = strings[1:]
                cleanUnits = []
                for unit in units:
                    cleanUnits.append(unit.strip(";").strip('\n'))
                # Setup the data frame
                column_names = ['station', 'latitude', 'longitude', 'elevation', 'ondate', 'offdate']
                for ilabel, label in enumerate(cleanLabels):
                    column_names.append(label + " " + cleanUnits[ilabel])
                siteTerms = np.zeros((nlines, len(cleanLabels)))
            elif count == 3:
                pass # don't care about the type because Python is stringy
            else:
                while("" in strings):
                    strings.remove("")     
                if strings == []:
                    break
                stations.append(strings[0])
                latitudes[nstation] = float(strings[1])
                longitudes[nstation] = float(strings[2])
                elevations[nstation] = float(strings[3])
                ondates[nstation] = int(strings[4])
                offdates[nstation] = int(strings[5])
                for ilabel, label in enumerate(cleanLabels):
                    siteTerms[nstation, ilabel] = float(strings[8+ilabel*4])
                nstation += 1
        # Creates the output DataFrame
        d = {"station": stations, "latitude": latitudes, "longitude": longitudes, "elevation": elevations,
                "ondate": ondates, "offdate": offdates}
        for icol, col in enumerate(column_names[6:]):
            d[col] = siteTerms[:,icol]
        df = pd.DataFrame(d, columns=column_names)
        return df
    
    @staticmethod
    def writeSiteTermsDFtoFile(df, fname):
        """
        Takes a DataFrame as read by getSiteTerms and then writes to a file, fname
        """
        attributes = df.columns[6:]
        cleanLabels = []
        cleanUnits = []
        for iatt, att in enumerate(attributes):
            tmp = att.split(" ")
            cleanLabels.append(tmp[0])
            cleanUnits.append(tmp[1])
        with open(fname, 'w') as fp:
            tmpstr = "attributes:"
            for ilabel, label in enumerate(cleanLabels):
                if ilabel < len(cleanLabels)-1:
                    tmpstr += " {};".format(label)
                else:
                    tmpstr += " {}\n".format(label)
            fp.write(tmpstr)
            tmpstr = "units:"
            for iunit, unit in enumerate(cleanUnits):
                if iunit < len(cleanUnits)-1:
                    tmpstr += " {};".format(unit)
                else:
                    tmpstr += " {}\n".format(unit)
            fp.write(tmpstr)
            fp.write("DOUBLE\n")
            for irow in range(len(df)):
                tmpstr = "{} {:>02.6f} {:>11.6f}\t{:>03.3f} {:>d} {:>d}".format(df['station'][irow],
                                                        df['latitude'][irow],
                                                        df['longitude'][irow],
                                                        df['elevation'][irow],
                                                        int(df['ondate'][irow]),
                                                        int(df['offdate'][irow]))
                
                for iatt, att in enumerate(attributes):
                    tmpstr += " {:4.3f}".format(df[att][irow])
                    
                fp.write(tmpstr)
                fp.write("\n")
        return
            
        
    @staticmethod
    def writeExtractNodes(data, fname, mode='3D'):
        assert mode in ['3D', '2D', 'grid'], "Error, mode must be 3D, 2D, or grid"
        if mode == '3D':
            with open(fname, "w") as fp:
                for idx, dat in enumerate(data[:-1]):
                    tmpstr = ""
                    for jdx, val in enumerate(dat):
                        if jdx != 3 and jdx != len(dat)-1:
                            if np.isfinite(val):
                                tmpstr += "{:>8.6f} ".format(val)
                            else:
                                tmpstr += "Infinity "
                        elif jdx == 3:
                            tmpstr += "{:d} ".format(int(val))
                        else:
                            tmpstr += "{}".format(val)
                    fp.write(tmpstr)
                    fp.write("\n")
        elif mode == '2D':
            with open(fname, "w") as fp:
                for idx, dat in enumerate(data[:-1]):
                    tmpstr = ""
                    for jdx, val in enumerate(dat):
                        if jdx != len(dat)-1:
                            if np.isfinite(val):
                                tmpstr += "{:>8.6f} ".format(val)
                            else:
                                tmpstr += "Infinity "
                        else:
                            tmpstr += "{}".format(val)
                    fp.write(tmpstr)
                    fp.write("\n")
        elif mode == 'grid':
            with open(fname, "w") as fp:
                for idx, dat in enumerate(data[:-1]):
                    tmpstr = "{:>8.6f} {:>8.6f}\n".format(dat[0], dat[1])
                    fp.write(tmpstr)
                    
                    
        
    def checkHorizontalInterpolation(self):
        if self.config['horizontalInterpolation'] is None:
            self.config['horizontalInterpolation'] = 'linear'
        elif self.config['horizontalInterpolation'] not in ['linear', 'nn', 'natural_neighbor']:
            self.config['horizontalInterpolation'] = 'linear'
    
    def checkRadialInterpolation(self):
        if self.config['radialInterpolation'] is None:
            self.config['radialInterpolation'] = 'linear'
        elif self.config['radialInterpolation'] not in ['linear', 'cs', 'cubic_spline']:
            self.config['radialInterpolation'] = 'linear'
        
    def setAttributeIndices(self):
        if isinstance(self.config['attributeIndices'], str):
            self.config['attributeIndices'] = self.config['attributeIndices'].replace(" ", "")
        elif isinstance(self.config['attributeIndices'], list): 
            tmp = ""
            for val in self.config['attributeIndices']:
                if val != self.config['attributeIndices'][-1]:
                    tmp += str(val) + ","
                else:
                    tmp += str(val)
            self.config['attributeIndices'] = tmp
        else:
            self.config['attributeIndices'] = None
          
    def setExtractActiveNodesParameters(self, attributeIndices=None, reciprocal=False, polygon=None):
        self.config['attributeIndices'] = attributeIndices
        self.setAttributeIndices() # Forces proper comma separated format
        self.config['reciprocal'] = reciprocal
        self.config['polygon'] = polygon
        # Need to write reading methods for 3D, 2D, and grid outputs

    def setResampleParameters(self, newGrid=None, newModelFile=None):
        self.config['newGrid'] = newGrid
        self.config['newModelFile'] = newModelFile

    def setUpdatedDescription(self, newModelFile = None, updatedDescription = None):
        self.config['newModelFile'] = newModelFile
        self.config['updatedDescription'] = updatedDescription

    def setSecondModel(self, secondModel = None, secondGridPath = None):
        self.config['secondModel'] = secondModel
        self.config['secondGridPath'] = secondGridPath

    def setModel(self, modelFile=None, gridPath = None):
        """
        Sets the file name for the model
        and the relative path for the grid directory (not used if grid stored in model file)

        Note that this method is used for multiple functions
        """
        self.config['modelFile'] = modelFile
        self.config['gridPath'] = gridPath

    def setExtractGridOutput(self, gridInputFile = None, gridOutputFile='stdout', gridLayTess = 0):
        """
        On execute, extractGrid will either write to file ending in "kml", "kmz", "vtk", "ascii", or binary
        pass gridOutputFile = 'stdout' or 'gmt' for loading the data into python after execution
        """
        self.config['gridInputFile'] = gridInputFile
        self.config['gridOutputFile'] = gridOutputFile
        self.config['gridLayTess'] = gridLayTess
        if gridOutputFile == 'stdout':
            self.config['gridOutputMode'] = 'stdout'
        elif gridOoutputFile == 'gmt':
            self.config['gridOutputMode'] = 'gmt'
        else:
            self.config['gridOoutputMode'] = 'file'


    def setFunction(self, function="help"):
        """
        Sets the configuration value "function" as one of the functions available in the geotess jar
        Run the help function for a full listing or choose from the following list:
            GeoTessExplorer 2.6.6

            Specify one of the following functions:
            version                 -- output the GeoTess version number
            toString                -- print summary information about a model
            updateModelDescription  -- update the description information for a model
            statistics              -- print summary statistics about the data in a model
            getClassName            -- discover the class name of a specified model
            equal                   -- given two GeoTessModels test that all radii and attribute values of all nodes are ==.  Metadata can differ.
            extractGrid             -- load a model or grid and write its grid to stdout, vtk, kml, ascii or binary file
            resample                -- resample a model onto a new grid
            extractActiveNodes      -- load a model and extract the positions of all active nodes
            replaceAttributeValues  -- replace the attribute values associated with all active nodes
            reformat                -- load a model and write it out in another format
            getValues               -- interpolate values at a single point
            getValuesFile           -- interpolate values at points specified in an ascii file
            interpolatePoint        -- interpolate values at a single point (verbose)
            borehole                -- interpolate values along a radial profile
            profile                 -- extract model values at vertex closest to specified latitude, longitude position
            findClosestPoint        -- find the closest point to a supplied geographic location and return information about it
            slice                   -- interpolate values on a vertical plane defined by a great circle connecting two points
            sliceDistAz             -- interpolate values on a vertical plane defined by a great circle defined by a point, a distance and a direction
            mapValuesDepth          -- interpolate values on a lat, lon grid at constant depths
            mapValuesLayer          -- interpolate values on a lat, lon grid at fractional radius in a layer
            mapLayerBoundary        -- depth of layer boundaries on a lat, lon grid
            mapLayerThickness       -- layer thickness on a lat, lon grid
            values3DBlock           -- interpolate values on a regular lat, lon, radius grid
            function                -- new model with attributes calculated from two input models
            vtkLayers               -- generate vtk plot file of values at the tops of layers
            vtkDepths               -- generate vtk plot file of values at specified depths
            vtkDepths2              -- generate vtk plot file of values at specified depths
            vtkLayerThickness       -- generate vtk plot file of layer thicknesses
            vtkLayerBoundary        -- generate vtk plot file of depth or elevation of layer boundary
            vtkSlice                -- generate vtk plot file of vertical slice
            vtkSolid                -- generate vtk plot file of entire globe
            vtk3DBlock              -- generate vtk plot file of values on a lat-lon-depth grid
            vtkPoints               -- generate vtk plot of point data
            vtkRobinson             -- generate vtk plot of a Robinson projection of model data
            vtkRobinsonLayers       -- generate vtk plot of a Robinson projection of model data at tops of multiple layers
            vtkRobinsonPoints       -- generate vtk plot of a Robinson projection of point data
            vtkRobinsonTriangleSize -- generate vtk plot of triangle size on Robinson projection
            vtkLayerAverage         -- generate vtk plot of the average values within the crust of a designated model
            reciprocalModel         -- generates a reciprocal GeoTessModel, where all values are inverted
            renameLayer             -- renames an individual layer in a GeoTessModel
            getLatitudes            -- array of equally spaced latitude values
            getLongitudes           -- array of equally spaced longitude values
            getDistanceDegrees      -- array of equally spaced distances along a great circle
            translatePolygon        -- translate polygon between kml/kmz and ascii format

            GeoTessModelSiteData:
            extractSiteTerms        -- extract site terms from a GeoTessModelSiteData and print to screen
            replaceSiteTerms        -- replace site terms in a GeoTessModelSiteData with values loaded from a file

            RSTT:
            extractPathDependentUncertaintyRSTT -- extract all the path dependent uncertainty information from a GeoTessModelSLBM
            replacePathDependentUncertaintyRSTT -- replace all the path dependent uncertainty information in a GeoTessModelSLBM
        """
        assert function in ['help', 'version', 'toString', 'updateModelDescription','statistics',
        'getClassName','equal','extractGrid','resample','extractActiveNodes','replaceAttributeValues',
        'reformat','getValues','getValuesFile','interpolatePoint','borehole','profile','findClosestPoint',
        'slice','sliceDistAz','mapValuesDepth','mapValuesLayer','mapLayerBoundary','mapLayerThickness','values3DBlock',
        'function', 'vtkLayers', 'vtkDepths', 'vtkDepths2', 'vtkLayerThickness', 'vtkLayerBoundary','vtkSlice','vtkSolid',
        'vtk3DBlock', 'vtkPoints', 'vtkRobinson', 'vtkRobinsonLayers', 'vtkRobinsonPoints', 'vtkRobinsonTriangleSize',
        'vtkLayerAverage', 'reciprocalModel', 'renameLayer', 'getLatitudes', 'getLongitudes', 'getDistanceDegrees',
        'translatePolygon','extractSiteTerms', 'replaceSiteTerms', 'extractPathDependentUncertaintyRSTT',
        'replacePathDependentUncertaintyRSTT'], "Error, please check available functions via help"

        self.config['function'] = function
        return

class ResultError:
    def __init__(self, stdout=None, stderr=None):
        self.stdout = stdout
        self.stderr = stderr
    def __str__(self):
        outstr = str(self.stdout) + " " + str(self.stderr)
        return outstr
    def __repr__(self):
        outstr = str(self.stdout) + " " + str(self.stderr)
        return outstr
    def setStdout(self, stdoutString):
        self.stdout = stdoutString
    def setStderr(self, stderrString):
        self.stderr = stderrString
    def appendStdout(self, tmpstr):
        if self.stdout is not None:
            self.stdout = self.stdout + "\n" + tmpstr
        else:
            self.stdout = tmpstr
    def appendStderr(self, tmpstr):
        if self.stderr is not None:
            self.stderr = self.stderr + "\n" + tmpstr
        else:
            self.stderr = tmpstr
    
