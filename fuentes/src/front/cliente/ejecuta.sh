#!/bin/bash
export JAVA_HOME=/all/tools/jdk-11.0.10+9
export PATH=$JAVA_HOME/bin:$PATH
#export CLASSPATH=glassfish-corba-internal-api-4.2.4.jar:glassfish-corba-omgapi-4.2.4.jar:glassfish-corba-orb-4.2.4.jar:pfl-basic-4.1.2.jar:pfl-dynamic-4.1.2.jar:pfl-tf-4.1.2.jar:gmbal-4.0.2.jar:webservices-rt-3.0.0.jar
export CLASSPATH=../lib/glassfish-corba-internal-api-4.2.4.jar:../lib/glassfish-corba-omgapi-4.2.4.jar:../lib/glassfish-corba-orb-4.2.4.jar:../lib/pfl-basic-4.1.2.jar:../lib/pfl-dynamic-4.1.2.jar:../lib/pfl-tf-4.1.2.jar:../lib/gmbal-4.0.2.jar:../lib/webservices-rt-3.0.0.jar:Filtro.jar

java -cp Cliente.jar:$CLASSPATH Cliente -ORBInitialPort 1050 -ORBInitialHost localhost
#java -cp Cliente.jar:../lib/glassfish-corba-internal-api-4.2.4.jar:../lib/glassfish-corba-omgapi-4.2.4.jar:../lib/glassfish-corba-orb-4.2.4.jar:../lib/pfl-basic-4.1.2.jar:../lib/pfl-dynamic-4.1.2.jar:../lib/pfl-tf-4.1.2.jar:../lib/gmbal-4.0.2.jar:../lib/webservices-rt-3.0.0.jar Cliente -ORBInitialPort 1050 -ORBInitialHost localhost