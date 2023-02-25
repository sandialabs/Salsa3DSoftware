"""
utilities.py
for use with Salsa3DSoftware python interfaces

Copyright 2023 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.

Rob Porritt
Sandia National Laboratories
February, 2023

"""

# utilities.py
# Set of additional functions used in projections into GeoTess
# Note that the default filenames are given in relative path to this software
# The functions that work on EMC NetCDF files require that the EMC model has been read by classes in:
#    EMC_NetCDF.py

import numpy as np
from scipy import interpolate

def read_ak135(filename='../Examples/data/AK135F_AVG_no_discon.csv'):
    """
    Reads a slightly modified version of AK135F as distributed from the IRIS EMC. The  included version adds 0.01 km to depths 
    when layers would overlap to avoid discontinuities.
    """
    f = open(filename,'r')
    lines = f.readlines()
    f.close()
    
    nlines = len(lines)
    depth = np.zeros((nlines,))
    radius = np.zeros((nlines,))
    vp = np.zeros((nlines,))
    vs = np.zeros((nlines,))
    rho = np.zeros((nlines,))
    for iline, l in enumerate(lines):
        a = l.split(',')
        depth[iline] = float(a[0])
        radius[iline] = 6371-depth[iline]
        rho[iline] = float(a[1])
        vp[iline] = float(a[2])
        vs[iline] = float(a[3])
        qk = float(a[4])
        qmu = float(a[5])
    return depth, radius, vp, vs, rho
    


# Reads the PREM model distributed as a comma separated value file from the IRIS EMC
def read_prem(filename='../Examples/data/PREM_1s.csv'):
    """
    Reads a csv formatted file and returns depth, radius, vp, vs, and density
    Columns from left to right represent radius,depth,density,Vpv,Vph,Vsv,Vsh,eta,Q-mu,Q-kappa
    Because here we're only concerned with isotropic, outputs are the averaged of Vpv, Vph and Vsv, Vsh
    """
    f = open(filename,'r')
    lines = f.readlines()
    f.close()

    nlines = len(lines)
    depth = np.zeros((nlines,))
    radius = np.zeros((nlines,))
    vp = np.zeros((nlines,))
    vs = np.zeros((nlines,))
    rho = np.zeros((nlines,))
    for iline, l in enumerate(lines):
        a = l.split(',')
        depth[iline] = float(a[1])
        radius[iline] = float(a[0])
        rho[iline] = float(a[2])
        vp[iline] = (float(a[3]) + float(a[4])) / 2.0
        vs[iline] = (float(a[5]) + float(a[6])) / 2.0
    return depth, radius, vp, vs, rho


# Reads the IASP91 velocity model as provided by the IRIS EMC
# Note that this model does not contain density.
def read_iasp91(filename='../Examples/data/IASP91.csv'):
    """
    Reads a csv formatted file and returns depth, radius, vp, vs
    """
    f = open(filename,'r')
    lines = f.readlines()
    f.close()

    nlines = len(lines)
    depth = np.zeros((nlines,))
    radius = np.zeros((nlines,))
    vp = np.zeros((nlines,))
    vs = np.zeros((nlines,))

    for iline, l in enumerate(lines):
        a = l.split(',')
        depth[iline] = float(a[0])
        radius[iline] = float(a[1])
        vp[iline] = float(a[2])
        vs[iline] = float(a[3])
    return depth, radius, vp, vs

def get_profile_emc_three_phase(longitude, latitude, depths, 
                       emc_mod,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mod_idx_p = 0, mod_idx_s = 1, mod_idx_rho=2,
                       convertFromPerturbation = True, EarthRadius=6371, unitSphere=True):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    emc_mod is from netcdf_to_geotess 
    set mod_idx_p and mod_idx_s for the order of the data arrays
    ref_x variables are from the read_prem() function
    Note that this is designed to populate a model that contains both a P model and an S model by sticking in the reference where no data is available
    Moreover, it also populates a density model from the reference (ref_rho)
    If convertFromPerturbation is True, assumes the model is given in percent from the reference 1D model
    """
    
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if emc_mod.interpFunction is None:
        emc_mod.prepareInterpolationFunction()
    vp_func = emc_mod.interpFunction[mod_idx_p]
    vs_func = emc_mod.interpFunction[mod_idx_s]
    rho_func = emc_mod.interpFunction[mod_idx_rho]
    
    # Check longitude being -180 to 180 or 0 to 360
    if np.max(emc_mod.longitude) > 190:
        if longitude < 0:
            longitude += 360
            
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    maxdepthp = np.max(emc_mod.depth)
    mindepthp = np.min(emc_mod.depth)
    minlonp = np.min(emc_mod.longitude)
    maxlonp = np.max(emc_mod.longitude)
    minlatp = np.min(emc_mod.latitude)
    maxlatp = np.max(emc_mod.latitude)
    
    maxdepths = np.max(emc_mod.depth)
    mindepths = np.min(emc_mod.depth)
    minlons = np.min(emc_mod.longitude)
    maxlons = np.max(emc_mod.longitude)
    minlats = np.min(emc_mod.latitude)
    maxlats = np.max(emc_mod.latitude)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        if dp <= maxdepthp and dp >= mindepthp and longitude >= minlonp and longitude <= maxlonp and latitude >= minlatp and latitude <= maxlatp:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vp_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vp_func(dp)
                vp[idp] = (vtmp * vref) + vref
            else:
                vp[idp] = vp_func([lontmp, lattmp, dptmp])[0]
        else:
            vp[idp] = ref_vp_func(dp)
            
        if dp <= maxdepths and dp >= mindepths and longitude >= minlons and longitude <= maxlons and latitude >= minlats and latitude <= maxlats:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vs_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vs_func(dp)
                vs[idp] = (vtmp * vref) + vref
            else:
                vs[idp] = vs_func([lontmp, lattmp, dptmp])[0]
        else:
            vs[idp] = ref_vs_func(dp)
            
        if dp <= maxdepths and dp >= mindepths and longitude >= minlons and longitude <= maxlons and latitude >= minlats and latitude <= maxlats:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = rho_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_rho_func(dp)
                rho[idp] = (vtmp * vref) + vref
            else:
                rho[idp] = rho_func([lontmp, lattmp, dptmp])[0]
        else:
            rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

def get_profile_emc_two_phase(longitude, latitude, depths, 
                       emc_mod,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mod_idx_p = 0, mod_idx_s = 1,
                       convertFromPerturbation = True, EarthRadius=6371, unitSphere=True, 
                       mindepthp = None, maxdepthp = None, mindepths = None, maxdepths = None):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    emc_mod is from netcdf_to_geotess 
    set mod_idx_p and mod_idx_s for the order of the data arrays
    ref_x variables are from the read_prem() function
    Note that this is designed to populate a model that contains both a P model and an S model by sticking in the reference where no data is available
    Moreover, it also populates a density model from the reference (ref_rho)
    If convertFromPerturbation is True, assumes the model is given in percent from the reference 1D model
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if emc_mod.interpFunction is None:
        emc_mod.prepareInterpolationFunction()
    vp_func = emc_mod.interpFunction[mod_idx_p]
    vs_func = emc_mod.interpFunction[mod_idx_s]
    
    # Check longitude being -180 to 180 or 0 to 360
    if np.max(emc_mod.longitude) > 190:
        if longitude < 0:
            longitude += 360
            
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    if maxdepthp is None:
        maxdepthp = np.max(emc_mod.depth)
    if mindepthp is None:
        mindepthp = np.min(emc_mod.depth)
    minlonp = np.min(emc_mod.longitude)
    maxlonp = np.max(emc_mod.longitude)
    minlatp = np.min(emc_mod.latitude)
    maxlatp = np.max(emc_mod.latitude)
    
    if maxdepths is None:
        maxdepths = np.max(emc_mod.depth)
    if mindepths is None:
        mindepths = np.min(emc_mod.depth)
    minlons = np.min(emc_mod.longitude)
    maxlons = np.max(emc_mod.longitude)
    minlats = np.min(emc_mod.latitude)
    maxlats = np.max(emc_mod.latitude)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        if dp <= maxdepthp and dp >= mindepthp and longitude >= minlonp and longitude <= maxlonp and latitude >= minlatp and latitude <= maxlatp:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vp_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vp_func(dp)
                vp[idp] = (vtmp * vref) + vref
            else:
                vp[idp] = vp_func([lontmp, lattmp, dptmp])[0]
        else:
            vp[idp] = ref_vp_func(dp)
            
        if dp <= maxdepths and dp >= mindepths and longitude >= minlons and longitude <= maxlons and latitude >= minlats and latitude <= maxlats:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vs_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vs_func(dp)
                vs[idp] = (vtmp * vref) + vref
            else:
                vs[idp] = vs_func([lontmp, lattmp, dptmp])[0]
        else:
            vs[idp] = ref_vs_func(dp)
            
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)



def get_profile_emc_vp_and_vs(longitude, latitude, depths, 
                       vp_mod, vs_mod,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mod_idx_p = 0, mod_idx_s = 0,
                       convertFromPerturbation = True, EarthRadius=6371, unitSphere=True):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    vp_mod and vp_mod are objects from netcdf_to_geotess 
      Note that vp_mod and vp_mod should be IRIS EMC model only containing a single dataArray, which is the VP or VS model
    ref_x variables are from the read_prem() function
    Note that this is designed to populate a model that contains both a P model and an S model by sticking in the reference where no data is available
    Moreover, it also populates a density model from the reference (ref_rho)
    If convertFromPerturbation is True, assumes the model is given in percent from the reference 1D model
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if vp_mod.interpFunction is None:
        vp_mod.prepareInterpolationFunction()
    if vs_mod.interpFunction is None:
        vs_mod.prepareInterpolationFunction()
    if vp_mod.nattributes > 1:
        vp_func = vp_mod.interpFunction[mod_idx_p]
    else:
        vp_func = vp_mod.interpFunction
    if vs_mod.nattributes > 1:
        vs_func = vs_mod.interpFunction[mod_idx_s]
    else:
        vs_func = vs_mod.interpFunction
    
    # Check longitude being -180 to 180 or 0 to 360
    if np.max(vp_mod.longitude) > 190:
        if longitude < 0:
            longitude += 360
            
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    maxdepthp = np.max(vp_mod.depth)
    mindepthp = np.min(vp_mod.depth)
    minlonp = np.min(vp_mod.longitude)
    maxlonp = np.max(vp_mod.longitude)
    minlatp = np.min(vp_mod.latitude)
    maxlatp = np.max(vp_mod.latitude)
    
    maxdepths = np.max(vs_mod.depth)
    mindepths = np.min(vs_mod.depth)
    minlons = np.min(vs_mod.longitude)
    maxlons = np.max(vs_mod.longitude)
    minlats = np.min(vs_mod.latitude)
    maxlats = np.max(vs_mod.latitude)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        if dp <= maxdepthp and dp >= mindepthp and longitude >= minlonp and longitude <= maxlonp and latitude >= minlatp and latitude <= maxlatp:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vp_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vp_func(dp)
                vp[idp] = (vtmp * vref) + vref
            else:
                vp[idp] = vp_func([lontmp, lattmp, dptmp])[0]
        else:
            vp[idp] = ref_vp_func(dp)
            
        if dp <= maxdepths and dp >= mindepths and longitude >= minlons and longitude <= maxlons and latitude >= minlats and latitude <= maxlats:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vs_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vs_func(dp)
                vs[idp] = (vtmp * vref) + vref
            else:
                vs[idp] = vs_func([lontmp, lattmp, dptmp])[0]
        else:
            vs[idp] = ref_vs_func(dp)
            
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

def get_profile_vp_only(longitude, latitude, depths, 
                       vp_mod, 
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mod_idx = 0,
                       convertFromPerturbation = True, EarthRadius=6371, unitSphere=True):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    vp_mod is an object from netcdf_to_geotess 
      Note that vp_mod should be an IRIS EMC model only containing a single dataArray, which is the VP model
    ref_x variables are from the read_prem() function
    Note that this is designed to populate a model that contains both a P model and an S model by sticking in the reference where no data is available
    Moreover, it also populates a density model from the reference (ref_rho)
    If convertFromPerturbation is True, assumes the model is given in percent from the reference 1D model
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if vp_mod.interpFunction is None:
        vp_mod.prepareInterpolationFunction()
        
    if vp_mod.nattributes > 1:
        vp_func = vp_mod.interpFunction[mod_idx_p]
    else:
        vp_func = vp_mod.interpFunction
    
    # Check longitude being -180 to 180 or 0 to 360
    if np.max(vp_mod.longitude) > 190:
        if longitude < 0:
            longitude += 360
    
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    maxdepths = np.max(vp_mod.depth)
    mindepths = np.min(vp_mod.depth)
    minlon = np.min(vp_mod.longitude)
    maxlon = np.max(vp_mod.longitude)
    minlat = np.min(vp_mod.latitude)
    maxlat = np.max(vp_mod.latitude)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        vs[idp] = ref_vs_func(dp)
        if dp <= maxdepths and dp >= mindepths and longitude >= minlon and longitude <= maxlon and latitude >= minlat and latitude <= maxlat:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vp_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vp_func(dp)
                vp[idp] = (vtmp * vref) + vref
            else:
                vp[idp] = vp_func([lontmp, lattmp, dptmp])[0]
        else:
            vp[idp] = ref_vp_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

def get_profile_vs_only(longitude, latitude, depths, 
                       vs_mod, 
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mod_idx = 0,
                       convertFromPerturbation = True, EarthRadius=6371, unitSphere=True):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    vs_mod is an object from netcdf_to_geotess 
      Note that vs_mod should only contain a single dataArray, which is the VS model
    ref_x variables are from the read_prem() function
    Note that this is designed to populate a model that contains both a P model and an S model by sticking in the reference where no data is available
    Moreover, it also populates a density model from the reference (ref_rho)
    If convertFromPerturbation is True, assumes the model is given in percent from the reference 1D model
    """

    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if vs_mod.interpFunction is None:
        vs_mod.prepareInterpolationFunction()

    if vs_mod.nattributes > 1:
        vs_func = vs_mod.interpFunction[mod_idx]
    else:
        vs_func = vs_mod.interpFunction
    
    # Check longitude being -180 to 180 or 0 to 360
    if np.max(vs_mod.longitude) > 190:
        if longitude < 0:
            longitude += 360
            
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    maxdepths = np.max(vs_mod.depth)
    mindepths = np.min(vs_mod.depth)
    minlon = np.min(vs_mod.longitude)
    maxlon = np.max(vs_mod.longitude)
    minlat = np.min(vs_mod.latitude)
    maxlat = np.max(vs_mod.latitude)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        vp[idp] = ref_vp_func(dp)
        if dp <= maxdepths and dp >= mindepths and longitude >= minlon and longitude <= maxlon and latitude >= minlat and latitude <= maxlat:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            if convertFromPerturbation:
                vtmp = vs_func([lontmp, lattmp, dptmp])[0] / 100.0
                vref = ref_vs_func(dp)
                vs[idp] = (vtmp * vref) + vref
            else:
                vs[idp] = vs_func([lontmp, lattmp, dptmp])[0]
        else:
            vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

# Defines a function that will produce a profile using the interpolation functions
# Note that this will only work if radius increases upward (ie array goes towards the surface)
def get_savani_profile(longitude, latitude, depths, 
                       savani_mod, 
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity'):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    savani_mod is an object from netcdf_to_geotess 
      Note that savani_mod should contain 4 interp functions for dvs, vsv, vsh, and xi
      This function gets the average vs from (vsv + vsh)/2
    ref_x variables are from the read_prem() function
    Note that this is designed to work with a model that contains both a P model and an S model
    Moreover, it also populates a density model from the reference (ref_rho)
    """

    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if savani_mod.interpFunction is None:
        savani_mod.prepareInterpolationFunction()
    fvsv = savani_mod.interpFunction[1]
    fvsh = savani_mod.interpFunction[2]
    
    # Longitude in savani_us is 0 to 360
    if longitude < 0:
        longitude += 360
    
    maxdepths = np.max(savani_mod.depth)
    mindepths = np.min(savani_mod.depth)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        vp[idp] = ref_vp_func(dp)
        if dp <= maxdepths and dp >= mindepths:
            vs[idp] = (fvsv([longitude, latitude, dp])[0] + fvsh([longitude, latitude, dp])[0])/2
        else:
            vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

def get_savani_profile_unitsphere(longitude, latitude, depths, 
                       savani_mod, 
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', EarthRadius=6371):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    savani_mod is an object from netcdf_to_geotess 
      Note that savani_mod should contain 4 interp functions for dvs, vsv, vsh, and xi
      This function gets the average vs from (vsv + vsh)/2
    ref_x variables are from the read_prem() function
    Note that this is designed to work with a model that contains both a P model and an S model
    Moreover, it also populates a density model from the reference (ref_rho)
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if savani_mod.interpFunction is None:
        savani_mod.prepareInterpolationFunction()
    fvsv = savani_mod.interpFunction[1]
    fvsh = savani_mod.interpFunction[2]
    
    # Longitude in savani_us is 0 to 360
    if longitude < 0:
        longitude += 360
    
    maxdepths = np.max(savani_mod.depth)
    mindepths = np.min(savani_mod.depth)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    lon = longitude * np.pi/180
    lat = latitude * np.pi/180
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        edp = dp / EarthRadius
        vp[idp] = ref_vp_func(dp)
        if dp <= maxdepths and dp >= mindepths:
            vs[idp] = (fvsv([lon, lat, edp])[0] + fvsh([lon, lat, edp])[0])/2
        else:
            vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)






# Defines a function that will produce a profile using the interpolation functions
# Note that this will only work if radius increases upward (ie array goes towards the surface)
def get_gypsum_profile(longitude, latitude, depths, 
                       pmod, smod, 
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', unitSphere=True, EarthRadius=6371):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    pmod and smod are objects from netcdf_to_geotess 
    ref_x variables are from the read_prem() function
    Note that this is designed to work with a model that contains both a P model and an S model
    Moreover, it also populates a density model from the reference (ref_rho)
    """

    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    
    if pmod.interpFunction is None:
        pmod.prepareInterpolationFunction()
    if smod.interpFunction is None:
        smod.prepareInterpolationFunction()
    
    maxdepthp = np.max(pmod.depth)
    maxdepths = np.max(smod.depth)
    
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    if unitSphere:
        ulon = longitude * np.pi/180
        ulat = latitude * np.pi/180
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        if dp < maxdepthp:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = dp
            vp[idp] = pmod.interpFunction([lontmp, lattmp, dptmp])[0]
        else:
            vp[idp] = ref_vp_func(dp)
        if dp < maxdepths:
            vs[idp] = smod.interpFunction([lontmp, lattmp, dptmp])[0]
        else:
            vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

def get_detox_profile_unitsphere(longitude, latitude, depths, 
                       pmod, maxdepthp,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mindepthp = 0, EarthRadius=6371):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    pmod is a 3D function that interpolates the underlying velocity model at the chosen point.
    Generate with code like:
        from scipy.interpolate import NearestNDInterpolator
        pmod = NearestNDInterpolator((longitude, latitude, radius), detox_abs_vel)
        
    This version converts longitude, latitude, and depths to unit sphere.
    Interpolation model, pmod, should be defined on a unit sphere with longitude wrapping
        
    maxdepthp forces the interpolator to use the reference model at depths greater than the set value.
        
    ref_x variables are from the read_iasp() function
    Note that rho, or density, is not available in IASP91.
    Therefore we can generate that from the PREM model:
            prem_depth, prem_rad, prem_vp, prem_vs, prem_rho = read_prem()
            prem_rho_func = interpolate.interp1d(prem_depth, prem_rho, kind='linear', fill_value=np.min(prem_rho), bounds_error=False)
            iasp_rho = prem_rho_func(iasp_depth)
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    lon = longitude * np.pi / 180.0
    lat = latitude * np.pi / 180.0
    
    for idp, dp in enumerate(depths):
        if dp < maxdepthp and dp > mindepthp:
            #print(dp, maxdepthp, mindepthp)
            rad = (EarthRadius-dp) / EarthRadius
            dvp = pmod(lon, lat, rad)
            vptmp = ref_vp_func(dp)
            vp[idp] = (dvp * vptmp) + vptmp
            #vp[idp] = pmod(lon, lat, rad)
        else:
            vp[idp] = ref_vp_func(dp)
        vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)




def get_detox_profile(longitude, latitude, depths, 
                       pmod, maxdepthp,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mindepthp = 0):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    pmod is a 3D function that interpolates the underlying velocity model at the chosen point.
    Generate with code like:
        from scipy.interpolate import NearestNDInterpolator
        pmod = NearestNDInterpolator((longitude, latitude, radius), detox_abs_vel)
        
    maxdepthp forces the interpolator to use the reference model at depths greater than the set value.
        
    ref_x variables are from the read_iasp() function
    Note that rho, or density, is not available in IASP91.
    Therefore we can generate that from the PREM model:
            prem_depth, prem_rad, prem_vp, prem_vs, prem_rho = read_prem()
            prem_rho_func = interpolate.interp1d(prem_depth, prem_rho, kind='linear', fill_value=np.min(prem_rho), bounds_error=False)
            iasp_rho = prem_rho_func(iasp_depth)
    """

    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value=np.min(ref_vp), bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value=np.min(ref_vs), bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value=np.min(ref_rho), bounds_error=False)
    
    for idp, dp in enumerate(depths):
        if dp < maxdepthp and dp > mindepthp:
            vp[idp] = pmod(longitude, latitude, 6371-dp)
        else:
            vp[idp] = ref_vp_func(dp)
        vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)


# Designed to work with the detox point cloud model where data are in vectors for 
# x or longitude position, y or latitude position, radii or radius position, dvp or velocity perturbation
# These are all Nx1 vectors
# vel_ref and rad_ref are Nx1 vectors that define the reference 1D velocity model
def convert_dvp_to_absolute(dvp, radii, vel_ref, rad_ref, mode='velocity', kind='linear', percent=False):
    """
    Given vector dvp with differential velocities (npts)
    defined on radii vector (npts)
    plus reference velocity vel_ref defined on radius rad_ref
    returns either the absolute velocity in km/s or slowness in s/km depending on 'mode'
    'kind' defines how the reference model is interpolated.
    """
    
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
    
    vel_ref_function = interpolate.interp1d(rad_ref, vel_ref, kind=kind, fill_value=np.min(vel_ref), bounds_error=False)
    
    vp_out = np.zeros((len(dvp),))
    
    for irad, rad in enumerate(radii):
        vp_ref_tmp = vel_ref_function(rad)
        if percent:
            vp_out[irad] = ((dvp[irad] / 100) * vp_ref_tmp) + vp_ref_tmp
        else:
            vp_out[irad] = (dvp[irad] * vp_ref_tmp) + vp_ref_tmp
        if vp_out[irad] < 0:
            print("vp_out[{}]: {}".format(irad, vp_out[irad]))
            print("Error, somehow we got a negative velocity.")
            return -1
        if vp_out[irad] >= np.finfo(float).eps and mode=='slowness':
            vp_out[irad] = 1.0 / vp_out[irad]
        elif mode == 'slowness':
            print("Error, slowness is too small or negative. {} {}".format(irad, vp_out[irad]))
            return -2
    return vp_out

def convert_dvp_to_absolute_unitsphere(dvp, radii, vel_ref, rad_ref, mode='velocity', kind='linear', percent=False, EarthRadius=6371):
    """
    Given vector dvp with differential velocities (npts)
    defined on radii vector (npts)
    plus reference velocity vel_ref defined on radius rad_ref
    returns either the absolute velocity in km/s or slowness in s/km depending on 'mode'
    'kind' defines how the reference model is interpolated.
    """
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
    
    vel_ref_function = interpolate.interp1d(rad_ref, vel_ref, kind=kind, fill_value=np.min(vel_ref), bounds_error=False)
    
    vp_out = np.zeros((len(dvp),))
    
    for irad, rad in enumerate(radii):
        # rad = unit sphere
        # erad = unit sphere * earthradius
        erad = EarthRadius * rad
        vp_ref_tmp = vel_ref_function(erad)
        if percent:
            vp_out[irad] = ((dvp[irad] / 100) * vp_ref_tmp) + vp_ref_tmp
        else:
            vp_out[irad] = (dvp[irad] * vp_ref_tmp) + vp_ref_tmp
        if vp_out[irad] < 0:
            print("vp_out[{}]: {}".format(irad, vp_out[irad]))
            print("Error, somehow we got a negative velocity.")
            return -1
        if vp_out[irad] >= np.finfo(float).eps and mode=='slowness':
            vp_out[irad] = 1.0 / vp_out[irad]
        elif mode == 'slowness':
            print("Error, slowness is too small or negative. {} {}".format(irad, vp_out[irad]))
            return -2
    return vp_out




# Reads the detox model file. This model is a point cloud with 2 header lines
# Points in the model are a cartesian mesh, which we can convert to lat/lon/rad via
# standard trigonometry
def read_detox_model(filename=None, convertToLatLon = True):
    """
    Reads a detox formatted point cloud and converts to a spherical point cloud
    File contains 2 header lines and then points in dx, dy, dz from center of earth
    """
    try:
        f = open(filename,'r')
        lines = f.readlines()[2:]
        f.close()
    except:
        print("Error, file {} not found".format(filename))
        return -1, -1, -1, -1
    
    nlines = len(lines)
    x = np.zeros((nlines,))
    y = np.zeros((nlines,))
    z = np.zeros((nlines,))
    dvp = np.zeros((nlines,))

    for iline, l in enumerate(lines):
        a = l.split()
        x[iline] = float(a[0])
        y[iline] = float(a[1])
        z[iline] = float(a[2])
        dvp[iline] = float(a[3])
    
    if convertToLatLon:
        radius = np.sqrt(x**2 + y**2 + z**2)
        colatitude = np.arccos(z/radius) * 180.0/np.pi
        longitude = np.arctan2(y, x) * 180.0/np.pi
        latitude = 90-colatitude
        return longitude, latitude, radius, dvp
    else:
        return x,y,z, dvp

    
# Reads the MIT08-P model
# Format has a single header line then columns for:
# latitude (-90 to 90 changing fastest)
# longitude (0 to 360 changing second)
# depth (22.6 to 2868.9 changing last)
# dvp (% relative to ak135)
def read_mit_model(filename=None):
    """
    # Reads the MIT08-P model
    # Format has a single header line then columns for:
    # latitude (-90 to 90 changing fastest)
    # longitude (0 to 360 changing second)
    # depth (22.6 to 2868.9 changing last)
    # dvp (% relative to ak135)
    """
    try:
        f = open(filename,'r')
        lines = f.readlines()[1:]
        f.close()
    except:
        print("Error, file {} not found.".format(filename))
        return -1, -1, -1, -1

    nlines = len(lines)
    latitude = np.zeros((nlines,))
    longitude = np.zeros((nlines,))
    depth = np.zeros((nlines,))
    radius = np.zeros((nlines,))
    dvp = np.zeros((nlines,))

    for iline, l in enumerate(lines):
        a = l.split()
        latitude[iline] = float(a[0])
        longitude[iline] = float(a[1])
        if longitude[iline] > 180:
            longitude[iline] -= 360
        depth[iline] = float(a[2])
        radius[iline] = 6371-depth[iline]
        dvp[iline] = float(a[3])
    
    return longitude, latitude, radius, dvp
    


# Given a vector of depths and definitions for the bottoms of layers and tops of layers (all in km)
# sorts the given depth vector into a list of lists that define the depth nodes in each layer
def define_layer_depths(depths, depthBottoms, depthTops):
    """
    Sets an array of depths from the input depths to layers based on depthTops and depthBottoms
    Importantly, output must close:
        The top of each layer must match the bottom of the upper layer
    """
    layerDepths = []
    for ilayer in range(len(depthTops)):
        tmpdepths = []
        for idp, dp in enumerate(depths):
            #print(dp, depthBottoms[ilayer], depthTops[ilayer], ilayer)
            if dp <= depthBottoms[ilayer] and dp >= depthTops[ilayer]:
                tmpdepths.append(dp)
        tmpdepths.append(depthBottoms[ilayer])
        tmpdepths.append(depthTops[ilayer])
        tmpdepths.sort()
        tmpdepths = np.unique(tmpdepths)
        tmpdepths = np.asarray(tmpdepths)
        layerDepths.append(tmpdepths)
    return layerDepths


def get_mit_profile(longitude, latitude, depths, 
                       pmod, maxdepthp,
                       ref_depth, ref_vp, ref_vs, ref_rho,
                       kind='linear', mode='velocity', mindepthp=0, unitSphere=True, EarthRadius=6371):
    """
    Creates a 1D profile at the point defined in latitude and longitude for an array of depths
    pmod is a 3D function that interpolates the underlying velocity model at the chosen point.
    Generate with code like:
        from scipy.interpolate import NearestNDInterpolator
        pmod = NearestNDInterpolator((longitude, latitude, radius), detox_abs_vel)
        
    maxdepthp forces the interpolator to use the reference model at depths greater than the set value.
        
    ref_x variables are from the read_ak135() function
    """
   
    assert kind in ['linear', 'nearest', 'nearest-up', 'zero', 'slinear', 'quadratic', 'cubic', 'previous', 'next'], 'error, kind not recognized.'
    assert mode in ['velocity', 'slowness'], 'error, mode must be velocity or slowness'
  
    vp = np.zeros((len(depths),))
    vs = np.zeros((len(depths),))
    rho = np.zeros((len(depths),))
    
    ref_vp_func = interpolate.interp1d(ref_depth, ref_vp, kind=kind, fill_value='extrapolate', bounds_error=False)
    ref_vs_func = interpolate.interp1d(ref_depth, ref_vs, kind=kind, fill_value='extrapolate', bounds_error=False)
    ref_rho_func = interpolate.interp1d(ref_depth, ref_rho, kind=kind, fill_value='extrapolate', bounds_error=False)
    
    ulon = np.pi * longitude / 180
    ulat = np.pi * latitude / 180
    
    for idp, dp in enumerate(depths):
        udp = dp / EarthRadius
        if dp < maxdepthp:
            if unitSphere:
                lontmp = ulon
                lattmp = ulat
                dptmp = 1-udp
            else:
                lontmp = longitude
                lattmp = latitude
                dptmp = 6371-dp
            vp[idp] = pmod(lontmp, lattmp, dptmp)
        else:
            vp[idp] = ref_vp_func(dp)
        vs[idp] = ref_vs_func(dp)
        rho[idp] = ref_rho_func(dp)
        
    if mode == 'slowness':
        for idp, dp in enumerate(depths):
            if vp[idp] <= 0.0:
                vp[idp] = 0.0
            else:
                vp[idp] = 1.0 / vp[idp]
            if vs[idp] <= 0.0:
                vs[idp] = 0.0
            else:
                vs[idp] = 1.0 / vs[idp]
    
    return np.flipud(6371-depths), np.flipud(np.asarray([vp, vs, rho]).T)

