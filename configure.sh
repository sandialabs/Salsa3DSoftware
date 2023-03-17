#!/bin/bash
set -e

# This script creates bash scripts in the current directory
# that will launch pcalc, locoo3d, geotess, geotessbuilder and suppormap

# if targte directory does not exist, abort
if [ ! -d $(pwd)/target ]; then
	echo ERROR: target directory does not exist
	echo ERROR: build the code first using: mvn clean package
	exit -1
fi

# find the full path to the salsa3d-software jar file located in the taraget directory
jarfile=$(pwd)/target/$(basename $(pwd)/target/$(find . -name 'salsa3d-software-*-jar-with-dependencies.jar'))

echo jarfile is $jarfile

# ---- 
echo "Creating executable script file geotess that launches GeoTessExplorer"
echo "#!/bin/bash" > geotess
echo "java -cp $jarfile gov.sandia.geotess.GeoTessExplorer \$*" >> geotess

chmod 777 geotess

# ---- 
echo "Creating executable script file geotessbuilder that launches GeoTessBuilderMain"
echo "#!/bin/bash" > geotessbuilder
echo "java -cp $jarfile gov.sandia.geotessbuilder.GeoTessBuilderMain \$*" >> geotessbuilder

chmod 777 geotessbuilder

# ---- PCalc
echo Creating executable script file pcalc that launches PCalc
echo "#!/bin/bash" > pcalc
echo "java -Xmx256g -classpath $jarfile  gov.sandia.gmp.pcalc.PCalc  \$*" >> pcalc

chmod 777 pcalc

# ---- LocOO3D
echo "Creating executable script file locoo3d that launches LocOO3D"
echo "#!/bin/bash" > locoo3d
echo "java -Xmx16g -classpath $jarfile gov.sandia.gmp.locoo3d.LocOO  \$*" >> locoo3d
chmod 777 locoo3d

# ---- Add to path
# The script also prints to screen recommended addition to 
# the user's .cshrc, .bash_profile, or .profile that will make the new
# executable available via the PATH.  No changes to the user's
# environment are actually made.

if [ `uname -s` = Darwin ]; then
	echo "Add this line to your .bash_profile"
	echo "export PATH=$(pwd):\$PATH"
else
	echo "Add this line to your .cshrc file"
	echo "set path=( $(pwd) \$path )"
fi
