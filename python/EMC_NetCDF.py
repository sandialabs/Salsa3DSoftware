"""
EMC_NetCDF.py

Reads a NetCDF file and creates a point cloud and interpolation functions


Created on Tue Nov 23 10:10:57 2021

@author: Robert Porritt, Sandia National Laboratories

# Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS).
# Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains
# certain rights in this software.

"""

import numpy as np
from scipy.io import netcdf as nc


class EMC_EarthModel():
    def __init__(self, filename = None):
        self.modelFileName = filename
        # Set of metadata stored with netcdf file:
        self.acknowledgment =""
        self.modelName = ""
        self.author_name = ""
        self.author_email = ""
        self.author_institution = ""
        self.author_url = ""
        self.comment = ""
        self.Conventions = ""
        self.dimensions = {}
        self.geospatial_lat_max = ""
        self.geospatial_lat_min = ""
        self.geospatial_lat_resolution = ""
        self.geospatial_lat_units = ""
        self.geospatial_lon_max = ""
        self.geospatial_lon_min = ""
        self.geospatial_lon_resolution = ""
        self.geospatial_lon_units = ""
        self.geospatial_vertical_max = ""
        self.geospatial_vertical_min = ""
        self.geospatial_vertical_positive = ""
        self.geospatial_vertical_units = ""
        self.history = ""
        self.id = ""
        self.keywods = ""
        self.maskandscale = ""
        self.Metadata_Conventions = ""
        self.NCO = ""
        self.reference = ""
        self.reference_pid = ""
        self.repository_institution = ""
        self.repository_name = ""
        self.repository_pid = ""
        self.summary = ""
        self.title = ""
        self.version_byte = ""
        # Above metadata will be loaded as appropriate to GeoTess metadata
        # Default EMC variables are depth, latitude, longitude, vp, and vs
        # There may be others and those will need to be adjusted in the keybank and then
        # functions such as netcdfToPointCloud which makes some assumptions about
        # variable types
        self.keyBank = ["depth", "radius", "latitude", "longitude", "vp", "vs"]
        self.nx = 0
        self.ny = 0
        self.nz = 0
        self.nattributes = 0
        self.dataArray = []
        self.latitude = []
        self.longitude = []
        self.depth = []

    def setModelFileName(self, filename):
        self.modelFileName = filename


    def addToKeyBank(self, key=None):
        """
        Use of this function should only be to add new properties like rho, density, Q, etc...
        """
        if key is not None:
            self.keyBank.append(key)


    def netcdfToPointCloud(self):
        if self.modelFileName is None:
            print("Error, must set model file name first!")
            return -404
        ds = nc.netcdf_file(self.modelFileName, 'r', mmap=False)
        # Store metadata
        self.modelName = ds.id
        self.author_name = ds.author_name
        self.acknowledgment = ds.acknowledgment
        self.author_email = ds.author_email
        self.author_institution = ds.author_institution
        self.author_url = ds.author_url
        self.comment = ds.comment
        self.Conventions = ds.Conventions
        self.dimensions = ds.dimensions # Note that this should match array sizes below
        self.geospatial_lat_max = ds.geospatial_lat_max
        self.geospatial_lat_min = ds.geospatial_lat_min
        self.geospatial_lat_resolution = ds.geospatial_lat_resolution
        self.geospatial_lat_units = ds.geospatial_lat_units
        self.geospatial_lon_max = ds.geospatial_lon_max
        self.geospatial_lon_min = ds.geospatial_lon_min
        self.geospatial_lon_resolution = ds.geospatial_lon_resolution
        self.geospatial_lon_units = ds.geospatial_lon_units
        self.geospatial_vertical_max = ds.geospatial_vertical_max
        self.geospatial_vertical_min = ds.geospatial_vertical_min
        self.geospatial_vertical_positive = ds.geospatial_vertical_positive
        self.geospatial_vertical_units = ds.geospatial_vertical_units
        self.history = ds.history
        self.id = ds.id
        self.keywords = ds.keywords
        self.maskandscale = ds.maskandscale
        self.Metadata_Conventions = ds.Metadata_Conventions
        self.NCO = ds.NCO
        self.reference = ds.reference
        self.reference_pid = ds.reference_pid
        self.repository_institution = ds.repository_institution
        self.repository_name = ds.repository_name
        self.repository_pid = ds.repository_pid
        self.summary = ds.summary
        self.title = ds.title
        self.version_byte = ds.version_byte
        self.xyz = False

        # Check dimensions and variables
        keysAvailable = []
        for key in self.keyBank:
            if key in ds.variables:
                keysAvailable.append(True)
            else:
                keysAvailable.append(False)
        variableDictionary = dict(zip(self.keyBank, keysAvailable))
        for key in self.keyBank:
            if variableDictionary[key]:
                if key.lower() == 'depth' or key.lower() == 'radius':
                    nz = ds.variables[key][:].size
                elif key.lower() == 'latitude':
                    ny = ds.variables[key][:].size
                elif key.lower() == 'longitude':
                    nx = ds.variables[key][:].size
                # Add new attribute here!!!!!!!!!!!!!!!!!!
                #if key.lower() == 'vp' or key.lower() == 'vs':
                else:
                    (nz2, ny2, nx2) = ds.variables[key][:].shape
        if nz != nz2:
            print("Error, number of vertical nodes inconsistent")
            return -1
        if ny != ny2:
            print("Error, number of latitude nodes inconsistent")
            return -2
        if nx != nx2:
            print("Error, number of longitude nodes inconsistent")
            return -3
        # Prepare a point cloud
        # Which is effectively a table of x, y, z, and value
        # If we were sure the data were on even grids, then it wouldn't be a problem
        # but we can't be sure of that
        # Or maybe we can?
        if variableDictionary['depth']:
            self.depth = ds.variables['depth'][:]
        elif variableDictionary['radius']:
            # Using a constant radius of 6371 is not quite right here, but will
            # generally be appropriate as the model ellipsoid is commonly unknwon
            self.depth = 6371 - ds.variables['radius'][:]
        self.latitude = ds.variables['latitude'][:] # Will need to change if latitdue key is not used
        self.longitude = ds.variables['longitude'][:] # same as latitude
        # Now we want the attributes
        self.nattributes = 0
        self.dataArray = []
        for key in self.keyBank:
            if key.lower() not in ["radius", "depth", "latitude", "longitude"]:
                if variableDictionary[key]:
                    self.nattributes += 1
                    self.dataArray.append(ds.variables[key][:])
        # Done, so close up the netcdf file
        ds.close()
        return 0

    def zxy2xyz(self):
        """
        Tries to convert the dataArray from zxy to
        """
        if not self.xyz:
            (nz, nx, ny) = self.dataArray[0].shape
            tmp = np.zeros((nx, ny, nz))
            for ix in range(nx):
                for iy in range(ny):
                    for iz in range(nz):
                        tmp[ix, iy, iz] = self.dataArray[0][iz, ix, iy]
            self.xyz = True
            return 1
        else:
            return -1

