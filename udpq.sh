#!/bin/bash
export GROOVY_HOME=/opt/groovy-home
export JAVA_HOME=/opt/java-home
export PATH=$GROOVY_HOME/bin:$JAVA_HOME/bin:/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin:/bin:/sbin
groovy udpq.groovy