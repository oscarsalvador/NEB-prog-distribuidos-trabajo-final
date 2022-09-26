#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

javac -cp $CLASSPATH -d target src/servidores/*.java

jar cf bin/servidor-central.jar -C target Central.class -C target 'Central$AtiendeAdmin.class' -C target 'Central$AtiendeMults.class' -C target 'Central$AtiendeProxy.class' -C target 'Central$MultiServ.class'
