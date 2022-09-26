Many of the examples in this directory specify the location of a Salsa3D model 
definition directory called salsa3d_model located in the top level Examples 
directory (the directory where this README.TXT file is located).  A simple way 
to handle this situation is to create a softlink from the Salsa3D model  
directory you already have on your system to the expected path. For example:

ln -s <path to Sals3D_v2.1> salsa3d_model
