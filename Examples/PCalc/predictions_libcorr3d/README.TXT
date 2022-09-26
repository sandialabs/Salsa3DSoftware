This example includes 4 properties files: pcalc_predictions_geotess.properties 
is similar in style to the other examples included in this package	and is 
suitable for running quickly.  It computes distance dependent uncertainty values 
and refers to the salsa3d_model in the top level Examples directory.
	
The other 3 properties files compute path dependent uncertainties.  They need to 
be run on a substantial computer and will take hours to run.  They also require 
some editing to refer to appropriate directories and should be carefully 
reviewed to ensure that the results they compute are appropriate.  They differ 
from each other in the manner in which the grids that support the computed 
libcorr3d models are managed.  See the PCalc Users Manual Appendix B and the 
individual properties files for more information.
