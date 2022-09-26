#!/bin/bash
# export JAVA_HOME=/all/tools/jdk-11.0.10+9
# export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=lib/*:bin/*

ps -ef|grep java |grep ORBInitialPort |awk '{ print $2 }'|xargs kill >/dev/null 2>&1
ps -ef|grep orbd |grep -v grep |awk '{ print $2 }'|xargs kill >/dev/null 2>&1

javac -cp $CLASSPATH -d target src/seguridad/*.java

jar cf bin/seguridad-autenticacion.jar -C target Autenticacion.class -C target Autenticacion\$AtiendeAdmin.class -C target Autenticacion\$AtiendeProxy.class
jar cf bin/seguridad-admin.jar -C target AdminUsuarios.class
cp src/seguridad/BBDDUsuarios.csv bin/