#!/bin/sh

# **************************************************
# ** Libraries used by Kettle:                    **
# **************************************************

BASEDIR=`dirname $0`
cd $BASEDIR

CLASSPATH=$BASEDIR
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-core.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-db.jar
CLASSPATH=$CLASSPATH:$BASEDIR/lib/kettle-engine.jar

# **************************************************
# ** JDBC & other libraries used by Kettle:       **
# **************************************************

for f in `find $BASEDIR/libext -type f -name "*.jar"` `find $BASEDIR/libext -type f -name "*.zip"`
do
  CLASSPATH=$CLASSPATH:$f
done


# **************************************************
# ** Platform specific libraries ...              **
# **************************************************

JAVA_BIN=java
LIBPATH="NONE"
GDAL_LIBPATH="NONE"
GDAL_DATA=libext/geometry/gdal_data

case `uname -s` in 
	Darwin)
		LIBPATH=$BASEDIR/libswt/osx/
		;;

	Linux)
	    ARCH=`uname -m`
		case $ARCH in
			x86_64)
				LIBPATH=$BASEDIR/libswt/linux/x86_64/
				GDAL_LIBPATH=$BASEDIR/libext/geometry/libgdal/linux/x86_64/
				;;

			i[3-6]86)
				LIBPATH=$BASEDIR/libswt/linux/x86/
				GDAL_LIBPATH=$BASEDIR/libext/geometry/libgdal/linux/x86/
				;;
		esac
		;;
esac 

export LIBPATH
export GDAL_LIBPATH
export GDAL_DATA

if [ "$GDAL_LIBPATH" != "NONE" ]
then
  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$GDAL_LIBPATH
fi

if [ "$LIBPATH" != "NONE" ]
then
  for f in `find $LIBPATH -name '*.jar'`
  do
    CLASSPATH=$CLASSPATH:$f
  done
fi


# circumvention for the IBM JVM behavior (seems to be a problem with the IBM JVM native compiler)
if [ `uname -s` = "OS400" ]
then
  CLASSPATH=${CLASSPATH}:$BASEDIR/libswt/aix/swt.jar
fi


# ******************************************************************
# ** Set java runtime options                                     **
# ** Change 128m to higher values in case you run out of memory.  **
# ******************************************************************

if [ -z "$JAVAMAXMEM" ]; then
  JAVAMAXMEM="256"
fi

if [ "$GDAL_LIBPATH" != "NONE" ]
then
  OPT="-Xmx${JAVAMAXMEM}m -cp $CLASSPATH -Djava.library.path=$LIBPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"
else
  OPT="-Xmx${JAVAMAXMEM}m -cp $CLASSPATH -DKETTLE_HOME=$KETTLE_HOME -DKETTLE_REPOSITORY=$KETTLE_REPOSITORY -DKETTLE_USER=$KETTLE_USER -DKETTLE_PASSWORD=$KETTLE_PASSWORD -DKETTLE_PLUGIN_PACKAGES=$KETTLE_PLUGIN_PACKAGES -DKETTLE_LOG_SIZE_LIMIT=$KETTLE_LOG_SIZE_LIMIT"
fi

if [ "$1" = "-x" ]; then
  set LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$BASEDIR/libext
  export LD_LIBRARY_PATH
  OPT="-Xruntracer $OPT"
  shift
fi

# ***************
# ** Run...    **
# ***************

$JAVA_BIN $OPT org.pentaho.di.pan.Pan "${1+$@}"

