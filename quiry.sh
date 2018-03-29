#!/bin/bash

#path to java
java_home=$JAVA_HOME

cygwin=false

case "`uname`" in
CYGWIN*) cygwin=true;
esac

# this part gets the program name
prog_name="$0"

# -h means symbolic link, in which case we get the link target
while [ -h $prog_name ]; do
  ls=`ls -ld $prog_name`
  link=`expr $ls : '.*-> \(.*\)$'`
  if expr $link : '.*/.*' > /dev/null; then
    prog_name=$link
  else
    prog_name=`dirname $prog_name`/$link
  fi
done

# get the path to this shell script so we can set the classpath
prog_dir=`dirname $prog_name`

if $cygwin; then
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

CLASSPATH="$CLASSPATH:$prog_dir/WEB-INF/classes"
for jar in $prog_dir/WEB-INF/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

if $cygwin; then
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
    CLASSPATH="$CLASSPATH;d:/tomcat/common/lib/servlet-api.jar"
fi

export CLASSPATH

if [ -d "$java_home" ]; then
    java_prog="$java_home/bin/java"
    # java_prog=java
else
    java_prog=java
fi

"$java_prog" -Xms64M -Xmx256M name.subroutine.quiry.Quiry "$@"
