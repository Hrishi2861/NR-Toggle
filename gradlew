#!/bin/sh

# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

##############################################################################
#
#   Gradle start up script for POSIX shells
#
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link;; # absolute symlink
      *)    app_path=$APP_HOME$link;; # relative symlink
    esac
done

APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
GRADLE_OPTS="${GRADLE_OPTS:--Xmx64m -Xms64m}"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$( uname -s )" = "Linux" ] || [ "$( uname -s )" = "Darwin" ] ; then
    MAX_FD_LIMIT=$( ulimit -H -n )
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD_LIMIT" != "unlimited" ] ; then
            ulimit -n $MAX_FD_LIMIT
        fi
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if [ "$( uname -s )" = "Darwin" ] ; then
    GRADLE_OPTS="$GRADLE_OPTS -Xdock:name=$APP_NAME -Xdock:icon=$APP_HOME/media/gradle.icns"
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" ] || [ "$msys" = "true" ] ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )
    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRS="$( cygpath --drive-letter )"
    if [ -n "$JAVA_HOME" ] ; then
        JAVAROOT="$JAVA_HOME"
        [ -n "$JAVAROOT" ] && ROOTDIRS="$ROOTDIRS|$( cygpath --drive-letter "$JAVAROOT" )"
    fi
    CYGPATTERN="^(($ROOTDIRS)[/\\\\]).*"
    for arg do
        if [ "$( cygpath --path --windows "$arg" 2>/dev/null )" != "" ] ; then
            arg=$( cygpath --path --windows "$arg" )
        elif [ "$( expr "$( cygpath --path --unix "$arg" 2>/dev/null )" : "$CYGPATTERN" )" != 0 ] ; then
            arg=$( cygpath --path --windows "$arg" )
        fi
        CYGPATTERN="$CYGPATTERN|$( echo "$arg" | sed 's/^\(.*\)[\\\/]/\1[\\\/]/' )"
        CMD_LINE_ARGS="$CMD_LINE_ARGS \"$arg\""
    done
fi

# Create classpath
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Execute Gradle
exec "$JAVACMD" $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
