#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

javac -cp $CLASSPATH -d target src/servidores/utils-multiservidor-anillo/*.java

jar cf bin/servidor-utils.jar -C target servidor/utils/Tarea.class -C target servidor/utils/NodoUso.class
