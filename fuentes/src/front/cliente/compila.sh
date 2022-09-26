#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

ps -ef|grep java |grep ORBInitialPort |awk '{ print $2 }'|xargs kill >/dev/null 2>&1
ps -ef|grep orbd |grep -v grep |awk '{ print $2 }'|xargs kill >/dev/null 2>&1

#genera carpeta peticionapp y mete todos los .class ahi
javac -cp $CLASSPATH -d target src/front/PeticionApp/*.java

jar cf bin/peticion.jar -C target PeticionApp

javac -cp $CLASSPATH -d target src/front/cliente/*.java

jar cf bin/front-cliente.jar -C target Cliente.class 