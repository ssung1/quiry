#!/bin/bash

cygwin=false

case "`uname`" in
CYGWIN*) cygwin=true;
esac

if $cygwin; then
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

CLASSPATH="$CLASSPATH:WEB-INF/classes:src"
for jar in WEB-INF/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

if $cygwin; then
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
    CLASSPATH="$CLASSPATH;/tomcat/lib/servlet-api.jar"
fi

export CLASSPATH

javafile=''

if [ $1 ]; then
    javafile="src/$1"
else
    javafile="src/name/subroutine/quiry/Resource.java"
fi

javac -deprecation -d WEB-INF/classes $javafile

cp -rp src/conf WEB-INF/classes
