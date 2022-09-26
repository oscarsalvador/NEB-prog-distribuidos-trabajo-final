#!/bin/bash
#export JAVA_HOME=/all/tools/jdk-11.0.10+9
#export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

ps -ef|grep java |grep ORBInitialPort |awk '{ print $2 }'|xargs kill >/dev/null 2>&1
ps -ef|grep orbd |grep -v grep |awk '{ print $2 }'|xargs kill >/dev/null 2>&1

#genera carpeta peticionapp y mete todos los .class ahi
javac -cp $CLASSPATH -d target src/front/PeticionApp/*.java

jar cf bin/peticion.jar -C target PeticionApp

javac -cp $CLASSPATH -d target src/front/proxy/*.java

jar cf bin/front-proxy.jar -C target Proxy.class -C target SyncAuth.class -C target PeticionImpl.class