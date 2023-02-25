"""
GeoTessBuilder.py

# Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
# Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
# certain rights in this software.

Class to build a geotess grid
Organizes properties and then sends a call to GeoTessBuilder
The C++ version of the library can't ingest kml/kmz files, so having the gui
build the parameter file and then call GeoTessBuilder will be the most
useful approach.


Created on Thu Nov 18 18:07:50 2021

@author: Rob Porritt

# Note that these define vertices in a tesselation layer
# specify a single point. The tokens in the property value are:
# 1) lat-lon, 2) tessellation index, 3) triangle edge length in degrees, # 4) latitude and 5) longitude. More points could have been
# specified by including similar strings, separated by semi-colons.
points = lat-lon, 0, 0.125, 31.88984, -36.000000
# Results in a coarse mesh globally, but refined mesh near the point
# Profiles that connect tesselations radially are defined elsewhere

# Version for a path using a kmz file
# specify a single path. The tokens in the property value are:
    # 1) the name of the file containing the path, 2) tessellation
    # index, and 3) triangle size for triangles near the path.
    paths = mid_atlantic_ridge.kmz, 0, 1.0
# Results in a coarse mesh throughout most of the sphere and 1.0 degree mesh near the
# mid-atlantic ridge

Polygons example for a kml file and a kmz file
polygons = \
    united_states.kml, 0, 1.0 ; \
    new_mexico.kmz, 0, 0.125

As of 01/31/2022: Euler rotation angle is not implemented


"""

import math
import subprocess


class GeoTessBuilder():
    def __init__(self, config=None):
        if config is None:
            self.GeoTessBuilderPropertiesFile = 'geotess_builder.properties'
            self.nTesselations = 1
            self.baseEdgeLengths = [8] # Must be powers of 2
            self.outputGridFile = 'uniform_tessellation.geotess'
            self.outputModelFile = 'refined_model.geotess'
            self.vtkFile = 'uniform_tessellation_%d.vtk'
            self.gridConstructionMode = 'Uniform' # Alternative is scratch or 'model refinement'
            self.initialSolid = 'icosahedron' # options are tetrahexahedron, octahedron, tetrahedron
            self.points = None
            self.paths = None
            self.polygons = None
            self.eulerRotationAngles = None
            self.propertyFileWritten = False
            self.jarfile = None
            self.modelToRefine = None
            self.fileOfPointsToRefine = None
            self.polygonToRefine = None
            self.captureExecution = True
        else:
            # Setting properties from config dictionary
            self.GeoTessBuilderPropertiesFile = config["GeoTessBuilderPropertiesFile"]
            self.nTesselations = config["nTesselations"]
            self.baseEdgeLengths = config["baseEdgeLengths"]
            self.outputGridFile = config["outputGridFile"]
            self.outputModelFile = config["outputModelFile"]
            self.vtkFile = config["vtkFile"]
            self.gridConstructionMode = config["gridConstructionMode"]
            self.initialSolid = config["initialSolid"]
            self.points = config['points']
            self.paths = config['paths']
            self.polygons = config['polygons']
            self.eulerRotationAngles = config['eulerRotationAngles']
            self.propertyFileWritten = config['propertyFileWritten']
            self.jarfile = config['jarfile']
            self.modelToRefine = config['modelToRefine']
            self.fileOfPointsToRefine = config['fileOfPointsToRefine']
            self.polygonToRefine = config['polygonToRefine']
            self.captureExecution = config['captureExecution']

    def writePropertiesFile(self):
        with open(self.GeoTessBuilderPropertiesFile, 'w') as propertyFile:
            if self.gridConstructionMode.lower() == 'scratch' or self.gridConstructionMode.lower() == 'uniform':
                propertyFile.write("gridConstructionMode = {}\n".format(self.gridConstructionMode))
                propertyFile.write("nTessellations = {:01.0f}\n".format(self.nTesselations))
                propertyFile.write("baseEdgeLengths =")
                if type(self.baseEdgeLengths) == list:
                    for ibel, bel in enumerate(self.baseEdgeLengths):
                        propertyFile.write(" {:01.5f}".format(bel))
                else:
                    propertyFile.write(" {:01.5f}".format(self.baseEdgeLengths))
                propertyFile.write("\n")
                if self.outputGridFile is not None:
                    propertyFile.write("outputGridFile = {}\n".format(self.outputGridFile))
                if self.vtkFile is not None:
                    propertyFile.write("vtkFile = {}\n".format(self.vtkFile))
                if self.initialSolid is not None:
                    propertyFile.write("initialSolid = {}\n".format(self.initialSolid))
                if self.points is not None:
                    propertyFile.write("points = \ \n")
                    for ipt, pt in enumerate(self.points):
                        propertyFile.write("{}, {}, {:02.5f}, {:02.5f}, {:02.5f} ; \ \n".format(pt[0], pt[1], pt[2], pt[3], pt[4]))
                    propertyFile.write("\n")
                if self.paths is not None:
                    propertyFile.write("paths = \ \n")
                    for ipath, pathlist in enumerate(self.paths):
                        propertyFile.write("{}, {}, {} ; \ \n".format(pathlist[0], pathlist[1], pathlist[2]))
                    propertyFile.write("\n")
                if self.polygons is not None:
                    propertyFile.write("polygons = \ \n")
                    for ipoly, poly in enumerate(self.polygons):
                        propertyFile.write("{}, {}, {} ; \ \n".format(poly[0], poly[1], poly[2]))
                    propertyFile.write("\n")
                if self.eulerRotationAngles is not None:
                    propertyFile.write("eulerRotationAngles = {}".format(self.eulerRotationAngles))
                    propertyFile.write("\n")

            else:
                propertyFile.write("gridConstructionMode = {}\n".format(self.gridConstructionMode))
                if self.modelToRefine is not None:
                    propertyFile.write("modelToRefine = {}\n".format(self.modelToRefine))
                if self.fileOfPointsToRefine is not None:
                    propertyFile.write("fileOfPointsToRefine = {}\n".format(self.fileOfPointsToRefine))
                if self.polygonToRefine is not None:
                    propertyFile.write("polygonToRefine = {}\n".format(self.polygonToRefine))
                if self.outputModelFile is not None:
                    propertyFile.write("outputModelFile = {}\n".format(self.outputModelFile))
                propertyFile.write("\n")

        self.propertyFileWritten = True

    def execute(self):
        result = -1
        if self.propertyFileWritten and self.jarfile is not None:
            st1 = 'java'
            st2 = '-cp'
            st3 = self.jarfile
            st4 = 'gov.sandia.geotessbuilder.GeoTessBuilderMain'
            st5 = self.GeoTessBuilderPropertiesFile
            cstrings = [st1, st2, st3, st4, st5]
            try:
                if self.captureExecution:
                    result = subprocess.run(cstrings, check=True, capture_output=True, text=True)
                    print(result.stdout)
                    print(result.stderr)
                else:
                    subprocess.run(cstrings, check=True)
                    result = 0
                
            except:
                print("Error running geotess builder. Check Jar file.")
                print("Jar file: {}".format(self.jarfile))
                result = -2
        else:
            print("Error executing. Need to set the jarfile and write the properties file first.")
            result = -3
        return result

    @staticmethod
    def initialize_configuration(GeoTessBuilderPropertiesFile='geotess_builder.properties', nTesselations=1, baseEdgeLengths=[8],
        outputGridFile = 'uniform_tessellation.geotess', outputModelFile='refined_model.geotess', vtkFile='uniform_tessellation_%d.vtk',
        gridConstructionMode='Uniform',initialSolid = 'icosahdron', points = None, paths = None, polygons= None, eulerRotationAngles = None,
        jarfile = None, modelToRefine = None, fileOfPointsToRefine = None, polygonToRefine = None, captureExecution = True, executionMode='Salsa3DSoftware'):
        """
        Returns a configuration dictionary for use in creating a new builder object
        """
        config = {
            "GeoTessBuilderPropertiesFile": GeoTessBuilderPropertiesFile,
            "nTesselations": nTesselations,
            "baseEdgeLengths": baseEdgeLengths,
            "outputGridFile": outputGridFile,
            "outputModelFile": outputModelFile,
            "vtkFile": vtkFile,
            "gridConstructionMode": gridConstructionMode,
            "initialSolid": initialSolid,
            "points": points,
            "paths": paths,
            "polygons": polygons,
            "eulerRotationAngles": eulerRotationAngles,
            "propertyFileWritten": False,
            "jarfile": jarfile,
            "modelToRefine": modelToRefine,
            "fileOfPointsToRefine": fileOfPointsToRefine,
            "polygonToRefine": polygonToRefine,
            "captureExecution": captureExecution
        }
        return config

    # default value is relative to python directory. Adjust to be relative to working directory.
    def setJarFile(self, jarfile='../target/salsa3d-software-1.2022.5-jar-with-dependencies.jar'):
        self.jarfile = jarfile

    def writeRefinementPointsFile(self, ptindex, fname='refinement_points.sb'):
        self.fileOfPointsToRefine = fname
        with open(self.fileOfPointsToRefine, 'w') as f:
            for pt in ptindex:
                f.write("{}\n".format(int(pt)))
            f.write("\n")

    def setRefinementModel(self, fname):
        """
        Just sets the modelToRefine attribute. Use with gridConstructionMode = 'model refinement'
        This needs to be a filename to a geotess model file, not a grid file.
        """
        self.modelToRefine = fname

    def setOutputModelFile(self, fname):
        self.outputModelFile = fname

    # Not necessary as these can be set once an object is created
    # but good practice as we can add error checking
    def setGeoTessBuilderPropertiesFile(self, fname):
        self.GeoTessBuilderPropertiesFile = fname

    def setNTesselations(self, nTess):
        self.nTesselations = nTess

    def setBaseEdgeLengths(self, baseEdgeLengths):
        self.baseEdgeLengths = baseEdgeLengths

    def setOutputGridFile(self, fname):
        self.outputGridFile = fname

    def setVTKFile(self, fname):
        self.vtkFile = fname

    def setCaptureMode(self, capture = False):
        self.captureExecution = capture

    def setEulerRotationAngles(self, eulerRotationAngles):
        """
        It is possible to rotate the triangular tessellation produced by GeoTessBuilder, relative to the traditional latitude, longitude grid, by providing 3 Euler rotations angles, in degrees. Given two coordinate systems xyz and XYZ with common origin, starting with the axis z and Z overlapping, the position of the second can be specified in terms of the first using three rotations with angles A, B, C as follows:
1. Rotate the xyz-system about the z-axis by A.
2. Rotate the xyz-system again about the now rotated x-axis by B.
3. Rotate the xyz-system a third time about the new z-axis by C.
Clockwise rotations, when looking in direction of vector, are positive (see http://mathworld.wolfram.com/EulerAngles.html).
For example, to rotate the grid such that grid vertex 0, which normally coincides with the north pole, resides instead at position geocentric_lat0, lon0 (in degrees), then supply the 3 Euler rotation angles lon0+90, 90 - geocentric_lat0, 0.
        """
        self.eulerRotationAngles = eulerRotationAngles

    def setGridConstructionMode(self, mode='Scratch'):
        assert mode.lower() in ['scratch', 'uniform', 'model refinement'], "Error, construction mode must be scratch or uniform"
        self.gridConstructionMode = mode

    def setInitialSolid(self, mode='icosahedron'):
        assert mode.lower() in ['icosahedron', 'tetrahexahedron', 'octahedron', 'tetrahedron'], "Error setting initial solid"
        self.initialSolid = mode

    def addPoint(self, tessID, edgeLength, latitude, longitude):
        if edgeLength > 64:
            edgeLength = 64
        pt = ['lat-lon', tessID, self.next_power_of_2(edgeLength), latitude, longitude]
        if self.points is None:
            self.points = []
        self.points.append(pt)

    def removePoint(self, ptIndex):
        newpoints = []
        for ipt, pt in enumerate(self.points):
            if ipt != ptIndex:
                newpoints.append(self.points[ipt])
        self.points = newpoints


    def addPath(self, filename, tessID, edgeLength):
        from os.path import exists
        # Really should check if the path is an appropriately formatted file...
        if exists(filename):
            if edgeLength > 64:
                edgeLength = 64
            tmpPath = [filename, tessID, edgeLength]
            if self.paths is None:
                self.paths = []
            self.paths.append(tmpPath)
        else:
            print("Error, file {} not found.".format(filename))

    def removePath(self, pathIndex):
        newpaths = []
        for ipt, pt in enumerate(self.paths):
            if ipt != pathIndex:
                newpaths.append(pt)
        self.paths = newpaths

    def addPolygon(self, filename, tessID, edgeLength):
        from os.path import exists
        # Really should check if the path is an appropriately formatted file...
        if exists(filename):
            if edgeLength > 64:
                edgeLength = 64
            tmpPoly = [filename, tessID, edgeLength]
            if self.polygons is None:
                self.polygons = []
            self.polygons.append(tmpPoly)
        else:
            print("Error, file {} not found.".format(filename))

    def removePolygon(self, polygonIndex):
        newpolygons = []
        for ipt, pt in enumerate(self.polygons):
            if ipt != polygonIndex:
                newpolygons.append(pt)
        self.polygons = newpolygons

    def next_power_of_2(self, x):
        return 1 if x == 0 else 2**math.ceil(math.log2(x))


class Polygon2D():
    def __init__(self, filename=None):
        self.filename = filename
        self.comments = None
        self.mode = 'lat-lon'
        self.reference = [0, 0, 'inside']
        self.points = None

    def write(self):
        with open(self.filename, 'w') as f:
            for comment in self.comments:
                f.write("# {}\n".format(comment))
            f.write("Polygon2D\n")
            f.write("Reference {} {} {}\n".format(self.reference[0], self.reference[1], self.reference[2]))
            f.write("{}\n".format(self.mode))
            if self.points is not None:
                for ipt, pt in enumerate(self.points):
                    f.write("{} {}\n".format(pt[0], pt[1]))

    def setReference(self, lat, lon, mode='inside'):
        assert mode in ["inside", "outside"], "Error, wrong mode in set reference. Must be inside or outside."
        if self.mode == 'lat-lon':
            self.reference = [lat, lon, mode]
        else:
            self.reference = [lon, lat, mode]

    def toggleMode(self, auto=True, mode='lat-lon'):
        """
        Consider this Texas and do not mess with it.
        This is to allow inputting values in lon-lat format rather than
        lat-lon, but this is likely to cause a can of worms.
        """
        if auto == True:
            if self.mode == "lat-lon":
                self.mode = "lon-lat"
            else:
                self.mode = "lat-lon"
        else:
            self.mode = mode

    def addPoint(self, lat, lon):
        if self.points is None:
            self.points = []
        if self.mode == 'lat-lon':
            self.points.append([lat, lon])
        else:
            self.points.append([lon, lat])

    def removePointIndex(self, index):
        newpoints = []
        for ipt, pt in enumerate(self.points):
            if ipt != index:
                newpoints.append(pt)
        self.points = newpoints

    def removePointLocation(self, lat, lon):
        newpoints = []
        for ipt, pt in enumerate(self.points):
            if self.mode == 'lat-lon':
                if pt[0] != lat or pt[1] != lon:
                    newpoints.append(pt)
            else:
                if pt[0] != lon or pt[1] != lat:
                    newpoints.append(pt)
        self.points = newpoints


    def setFilename(self, filename):
        self.filename = filename

    def addComment(self, commentString):
        if self.comments is None:
            self.comments = []
        self.comments.append(commentString)

    def removeComment(self, index):
        newComments = []
        for ic, c in enumerate(self.comments):
            if ic != index:
                newComments.append(c)
        self.comments = newComments


class Polygon3D():
    def __init__(self, filename=None):
        self.filename = filename
        self.comments = None
        self.mode = 'lat-lon'
        self.reference = [0, 0, 'inside']
        self.points = None
        self.top = None
        self.bottom = None

    def write(self):
        assert self.top is not None and self.bottom is not None, 'Error, must set top and bottom of 3D polygon!'
        with open(self.filename, 'w') as f:
            for comment in self.comments:
                f.write("# {}\n".format(comment))
            f.write("Polygon3D\n")
            f.write("{} {} {} {}\n".format(self.top[0], self.top[1], self.top[2], self.top[3]))
            f.write("{} {} {} {}\n".format(self.bottom[0], self.bottom[1], self.bottom[2], self.bottom[3]))
            f.write("Reference {} {} {}\n".format(self.reference[0], self.reference[1], self.reference[2]))
            f.write("{}\n".format(self.mode))
            if self.points is not None:
                for ipt, pt in enumerate(self.points):
                    f.write("{} {}\n".format(pt[0], pt[1]))

    def setTop(self, Z, layerIndex=-1, mode='radius'):
        assert mode in ["radius", "depth", "layer"], "Error, top mode must be radius, depth, or layer"
        self.top = ["TOP", mode, Z, layerIndex]

    def setBottom(self, Z, layerIndex=-1, mode='radius'):
        assert mode in ["radius", "depth", "layer"], "Error, bottom mode must be radius, depth, or layer"
        self.bottom = ["BOTTOM", mode, Z, layerIndex]

    def setReference(self, lat, lon, mode='inside'):
        assert mode in ["inside", "outside"], "Error, wrong mode in set reference. Must be inside or outside."
        if self.mode == 'lat-lon':
            self.reference = [lat, lon, mode]
        else:
            self.reference = [lon, lat, mode]

    def toggleMode(self, auto=True, mode='lat-lon'):
        """
        Consider this Texas and do not mess with it.
        This is to allow inputting values in lon-lat format rather than
        lat-lon, but this is likely to cause a can of worms.
        """
        if auto == True:
            if self.mode == "lat-lon":
                self.mode = "lon-lat"
            else:
                self.mode = "lat-lon"
        else:
            self.mode = mode

    def addPoint(self, lat, lon):
        if self.points is None:
            self.points = []
        if self.mode == 'lat-lon':
            self.points.append([lat, lon])
        else:
            self.points.append([lon, lat])

    def removePointIndex(self, index):
        newpoints = []
        for ipt, pt in enumerate(self.points):
            if ipt != index:
                newpoints.append(pt)
        self.points = newpoints

    def removePointLocation(self, lat, lon):
        newpoints = []
        for ipt, pt in enumerate(self.points):
            if self.mode == 'lat-lon':
                if pt[0] != lat or pt[1] != lon:
                    newpoints.append(pt)
            else:
                if pt[0] != lon or pt[1] != lat:
                    newpoints.append(pt)
        self.points = newpoints


    def setFilename(self, filename):
        self.filename = filename

    def addComment(self, commentString):
        if self.comments is None:
            self.comments = []
        self.comments.append(commentString)

    def removeComment(self, index):
        newComments = []
        for ic, c in enumerate(self.comments):
            if ic != index:
                newComments.append(c)
        self.comments = newComments
